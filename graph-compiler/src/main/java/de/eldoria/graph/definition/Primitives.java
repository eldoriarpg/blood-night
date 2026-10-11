package de.eldoria.graph.definition;

import java.util.Map;
import java.util.Optional;

/**
 * Boxing and unboxing between primitive classes and their wrappers.
 */
final class Primitives {
    private static final Map<Class<?>, Class<?>> BOXES = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            char.class, Character.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class);

    private Primitives() {
    }

    /**
     * @param type a primitive class
     * @return its wrapper, or the type itself when it is not primitive
     */
    static Class<?> box(Class<?> type) {
        return BOXES.getOrDefault(type, type);
    }

    /**
     * @param type a wrapper class
     * @return the primitive it wraps, if any
     */
    static Optional<Class<?>> unbox(Class<?> type) {
        return BOXES.entrySet().stream()
                .filter(entry -> entry.getValue() == type)
                .<Class<?>>map(Map.Entry::getKey)
                .findFirst();
    }
}
