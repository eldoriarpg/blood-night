/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import java.util.List;
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

    private static final List<Class<?>> NUMBERS =
            List.of(byte.class, short.class, int.class, long.class, float.class, double.class);

    private Primitives() {}

    /**
     * @param type a class
     * @return the primitive number type it is or wraps, if any
     */
    static Optional<Class<?>> number(Class<?> type) {
        Class<?> primitive = unbox(type).orElse(type);
        return NUMBERS.contains(primitive) ? Optional.of(primitive) : Optional.empty();
    }

    /**
     * @param a a primitive number type
     * @param b a primitive number type
     * @return the wider of both, e.g. {@code double} for {@code int} and {@code double}
     */
    static Class<?> wider(Class<?> a, Class<?> b) {
        return NUMBERS.indexOf(a) >= NUMBERS.indexOf(b) ? a : b;
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
