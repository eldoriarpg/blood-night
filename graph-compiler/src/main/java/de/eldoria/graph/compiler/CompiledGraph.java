package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;

import java.util.List;

/**
 * A graph compiled and loaded into its own class loader.
 *
 * @param name     name of the generated class
 * @param source   the generated source, for debugging
 * @param result   the loaded class
 * @param host     interface the class implements
 * @param warnings problems that did not prevent compilation
 * @param <T>      the host type
 */
public record CompiledGraph<T>(ClassName name, JavaFile source, CompilationResult result, Class<T> host, List<GraphProblem> warnings) {

    /**
     * Creates an instance for a platform with a context type.
     *
     * @param context the context object, e.g. the mob the behaviour belongs to
     * @return a new instance with its own state
     */
    public T instantiate(Object context) {
        return result.instantiate(name.reflectionName(), host, context);
    }

    /**
     * Creates an instance for a platform without a context type.
     *
     * @return a new instance with its own state
     */
    public T instantiate() {
        return result.instantiate(name.reflectionName(), host);
    }
}
