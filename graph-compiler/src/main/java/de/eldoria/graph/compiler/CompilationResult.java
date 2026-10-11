package de.eldoria.graph.compiler;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;

/**
 * Classes produced by one call to {@link RuntimeCompiler#compile}.
 *
 * @param loader  class loader holding the generated classes
 * @param sources the compiled sources, kept for debugging
 * @param took    wall time of the compilation
 */
public record CompilationResult(GeneratedClassLoader loader, List<SourceFile> sources, Duration took) {

    /**
     * Loads a generated class and checks it against the expected type.
     *
     * @param binaryName binary name of the class
     * @param type       type the class has to implement
     * @param <T>        expected type
     * @return the class
     */
    public <T> Class<? extends T> load(String binaryName, Class<T> type) {
        try {
            return Class.forName(binaryName, true, loader).asSubclass(type);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("No generated class " + binaryName, e);
        }
    }

    /**
     * Loads a generated class and calls its constructor.
     *
     * @param binaryName binary name of the class
     * @param type       type the class has to implement
     * @param arguments  constructor arguments, matched by their runtime classes against the parameter types
     * @param <T>        expected type
     * @return the new instance
     */
    public <T> T instantiate(String binaryName, Class<T> type, Object... arguments) {
        Class<? extends T> clazz = load(binaryName, type);
        for (var constructor : clazz.getConstructors()) {
            if (!accepts(constructor.getParameterTypes(), arguments)) continue;
            try {
                return type.cast(constructor.newInstance(arguments));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not create " + binaryName, e);
            }
        }
        throw new IllegalArgumentException("No matching constructor on " + binaryName);
    }

    private static boolean accepts(Class<?>[] parameters, Object[] arguments) {
        if (parameters.length != arguments.length) return false;
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].isInstance(arguments[i])) return false;
        }
        return true;
    }
}
