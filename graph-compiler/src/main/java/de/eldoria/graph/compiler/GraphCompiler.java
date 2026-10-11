package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.platform.GraphPlatform;

import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turns graphs into loaded classes: expand macros, validate, emit, compile.
 * <p>
 * Every stage reports problems located at nodes of the graph as stored. Problems inside a macro are reported on the
 * node using it, with a trace into the macro body. Errors the Java compiler finds in generated code are mapped back
 * to the node whose code contains them; such errors point at a faulty node definition rather than at the graph.
 *
 * @param <T> the platform's host type
 */
public final class GraphCompiler<T> {
    private final GraphPlatform platform;
    private final Class<T> host;
    private final RuntimeCompiler compiler;
    private final MacroLibrary macros;

    /**
     * @param platform definitions and host
     * @param host     the platform's host interface, for typed instances
     * @param parent   class loader that can load the host, the context and everything generated code refers to
     */
    public GraphCompiler(GraphPlatform platform, Class<T> host, ClassLoader parent) {
        this(platform, host, parent, MacroLibrary.empty());
    }

    /**
     * @param platform definitions and host
     * @param host     the platform's host interface, for typed instances
     * @param parent   class loader that can load the host, the context and everything generated code refers to
     * @param macros   macros graphs may use, compiled against the same platform
     */
    public GraphCompiler(GraphPlatform platform, Class<T> host, ClassLoader parent, MacroLibrary macros) {
        if (platform.host() != host) throw new IllegalArgumentException("Platform host is " + platform.host().getName());
        this.platform = platform;
        this.host = host;
        this.compiler = new RuntimeCompiler(platform.classpath(), parent);
        this.macros = macros;
    }

    /**
     * Expands, validates and emits a graph without compiling it.
     *
     * @param graph the graph
     * @param name  name of the generated class
     * @return the source
     * @throws GraphCompilationException when the graph is invalid
     */
    public JavaFile generate(Graph graph, ClassName name) throws GraphCompilationException {
        MacroExpander.Expansion expansion = MacroExpander.expand(graph, macros.definitions());
        return emit(expansion, validate(expansion), name).file();
    }

    /**
     * Expands, validates, emits, compiles and loads a graph.
     *
     * @param graph the graph
     * @param name  name of the generated class
     * @return the loaded class
     * @throws GraphCompilationException when the graph is invalid or the generated code does not compile
     */
    public CompiledGraph<T> compile(Graph graph, ClassName name) throws GraphCompilationException {
        MacroExpander.Expansion expansion = MacroExpander.expand(graph, macros.definitions());
        ValidatedGraph validated = validate(expansion);
        GraphEmitter.Emitted emitted = emit(expansion, validated, name);
        try {
            CompilationResult result = compiler.compile(emitted.file());
            return new CompiledGraph<>(name, emitted.file(), result, host, expansion.locate(validated.warnings()));
        } catch (CompilationException e) {
            List<GraphProblem> problems = new ArrayList<>(validated.warnings());
            e.diagnostics().stream()
                    .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                    .forEach(diagnostic -> problems.add(GraphProblem.error(
                            emitted.sourceMap().nodeAt(diagnostic.getLineNumber()).orElse(null), null,
                            "Generated code does not compile, line %d: %s".formatted(diagnostic.getLineNumber(), diagnostic.getMessage(Locale.ROOT)))));
            throw new GraphCompilationException(expansion.locate(problems), emitted.file().toString());
        }
    }

    private ValidatedGraph validate(MacroExpander.Expansion expansion) throws GraphCompilationException {
        try {
            return new GraphValidator(platform, MacroBoundary.passDefinitions(platform.types())).validate(expansion.graph());
        } catch (GraphCompilationException e) {
            throw new GraphCompilationException(expansion.locate(e.problems()), null);
        }
    }

    private GraphEmitter.Emitted emit(MacroExpander.Expansion expansion, ValidatedGraph validated, ClassName name) throws GraphCompilationException {
        try {
            return new GraphEmitter(validated).emit(name);
        } catch (GraphCompilationException e) {
            throw new GraphCompilationException(expansion.locate(e.problems()), null);
        }
    }
}
