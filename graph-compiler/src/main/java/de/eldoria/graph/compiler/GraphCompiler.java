/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.platform.GraphPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.tools.Diagnostic;

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
        if (platform.host() != host)
            throw new IllegalArgumentException(
                    "Platform host is " + platform.host().getName());
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
        return emit(expansion, validate(expansion, Map.of()), name, true).file();
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
        return compile(graph, name, Map.of());
    }

    /**
     * Expands, validates, emits, compiles and loads a graph with some of the platform's graph-wide type variables
     * bound, e.g. the type of the object it runs for.
     *
     * @param graph    the graph
     * @param name     name of the generated class
     * @param bindings types of graph-wide variables (see {@link de.eldoria.graph.platform.Binding}); the others take
     *                 their fallback
     * @return the loaded class
     * @throws GraphCompilationException when the graph is invalid or the generated code does not compile
     */
    public CompiledGraph<T> compile(Graph graph, ClassName name, Map<String, PortType> bindings)
            throws GraphCompilationException {
        MacroExpander.Expansion expansion = MacroExpander.expand(graph, macros.definitions());
        ValidatedGraph validated = validate(expansion, bindings);
        GraphEmitter.Emitted emitted = emit(expansion, validated, name, true);
        try {
            CompilationResult result = compiler.compile(emitted.file());
            return new CompiledGraph<>(
                    name, emitted.file().toString(), result, host, expansion.locate(validated.warnings()));
        } catch (CompilationException inlined) {
            GraphEmitter.Emitted plain = emit(expansion, validated, name, false);
            try {
                compiler.compile(plain.file());
                throw failure(expansion, validated, emitted, inlined);
            } catch (CompilationException e) {
                throw failure(expansion, validated, plain, e);
            }
        }
    }

    /**
     * Turns compiler errors into problems on the nodes whose code they are in.
     */
    private static GraphCompilationException failure(
            MacroExpander.Expansion expansion,
            ValidatedGraph validated,
            GraphEmitter.Emitted emitted,
            CompilationException e) {
        List<GraphProblem> problems = new ArrayList<>(validated.warnings());
        e.diagnostics().stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .forEach(diagnostic -> problems.add(GraphProblem.error(
                        emitted.sourceMap().nodeAt(diagnostic.getLineNumber()).orElse(null),
                        null,
                        "Generated code does not compile, line %d: %s"
                                .formatted(diagnostic.getLineNumber(), diagnostic.getMessage(Locale.ROOT)))));
        return new GraphCompilationException(
                expansion.locate(problems), emitted.file().toString());
    }

    /**
     * Restores a graph compiled earlier, without validating or compiling again. The caller is responsible for the
     * bytecode matching the current graph, platform and macros, e.g. by keying a cache on all three.
     *
     * @param name     name of the generated class
     * @param source   its source
     * @param bytecode its class files by binary name
     * @param warnings warnings found when it was compiled
     * @return the loaded class
     */
    public CompiledGraph<T> load(
            ClassName name, String source, Map<String, byte[]> bytecode, List<GraphProblem> warnings) {
        CompilationResult result = compiler.load(bytecode, List.of(SourceFile.of(name.reflectionName(), source)));
        return new CompiledGraph<>(name, source, result, host, warnings);
    }

    private ValidatedGraph validate(MacroExpander.Expansion expansion, Map<String, PortType> bindings)
            throws GraphCompilationException {
        try {
            return new GraphValidator(platform, MacroBoundary.passDefinitions(platform.types()), bindings)
                    .validate(expansion.graph());
        } catch (GraphCompilationException e) {
            throw new GraphCompilationException(expansion.locate(e.problems()), null);
        }
    }

    /**
     * @param inline whether values read once are written into their reader; without, every value is a local behind its
     *               own marker, which is what compiler errors are traced through when the inlined code fails
     */
    private GraphEmitter.Emitted emit(
            MacroExpander.Expansion expansion, ValidatedGraph validated, ClassName name, boolean inline)
            throws GraphCompilationException {
        try {
            return new GraphEmitter(validated, inline).emit(name);
        } catch (GraphCompilationException e) {
            throw new GraphCompilationException(expansion.locate(e.problems()), null);
        }
    }
}
