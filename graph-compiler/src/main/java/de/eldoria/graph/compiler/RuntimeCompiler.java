package de.eldoria.graph.compiler;

import com.palantir.javapoet.JavaFile;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Compiles generated Java source in memory and loads the result into a child class loader.
 * <p>
 * Uses the embedded Eclipse compiler, so compiling works on a JRE as well as on a JDK. The JDK's own classes are read
 * from the running runtime; everything else the generated code refers to has to be on the given classpath, because the
 * compiler cannot look into foreign class loaders.
 * <p>
 * Every call to {@link #compile(JavaFile...)} produces a fresh {@link GeneratedClassLoader}. Dropping every reference
 * to it unloads the generated classes.
 */
public final class RuntimeCompiler {
    /**
     * Java language level of generated code.
     */
    public static final int RELEASE = 25;

    private final List<Path> classpath;
    private final ClassLoader parent;

    /**
     * Creates a compiler.
     *
     * @param classpath jars and directories the generated code compiles against
     * @param parent    class loader that resolves those classes at runtime
     */
    public RuntimeCompiler(List<Path> classpath, ClassLoader parent) {
        this.classpath = List.copyOf(classpath);
        this.parent = parent;
    }

    /**
     * Compiles the files and loads the result.
     *
     * @param files files to compile together
     * @return the loaded classes
     * @throws CompilationException when the compiler reports errors
     */
    public CompilationResult compile(JavaFile... files) throws CompilationException {
        long start = System.nanoTime();
        JavaCompiler compiler = new EclipseCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<SourceFile> sources = Arrays.stream(files).map(SourceFile::of).toList();

        try (StandardJavaFileManager standard = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8);
             InMemoryFileManager fileManager = new InMemoryFileManager(standard)) {
            standard.setLocationFromPaths(StandardLocation.CLASS_PATH, classpath);
            standard.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of());
            List<String> options = List.of("-source", String.valueOf(RELEASE), "-target", String.valueOf(RELEASE),
                    "-proc:none", "-nowarn", "-encoding", "UTF-8");
            boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, sources).call();
            if (!success) throw new CompilationException(diagnostics.getDiagnostics(), sources);
            GeneratedClassLoader loader = new GeneratedClassLoader(fileManager.bytecode(), parent);
            return new CompilationResult(loader, sources, Duration.ofNanos(System.nanoTime() - start));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
