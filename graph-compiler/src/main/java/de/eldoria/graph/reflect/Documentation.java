/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Help texts for derived nodes, usually taken from javadoc. Reflection alone has no descriptions; a consumer that has
 * the sources of its API, e.g. at build time, can supply them here.
 */
public interface Documentation {
    /**
     * No texts at all.
     */
    Documentation NONE = new Documentation() {
        @Override
        public Optional<String> of(Class<?> type) {
            return Optional.empty();
        }

        @Override
        public Optional<String> of(Method method) {
            return Optional.empty();
        }
    };

    /**
     * @param type a class
     * @return a short description of the class, if documented
     */
    Optional<String> of(Class<?> type);

    /**
     * @param method a method
     * @return a short description of the method, if documented; implementations may fall back to overridden methods
     */
    Optional<String> of(Method method);
}
