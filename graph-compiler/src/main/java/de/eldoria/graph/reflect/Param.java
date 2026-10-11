/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import java.util.Objects;

/**
 * A parameter of a constructor or factory exposed as a node, with the input id the node gives it. Compiled APIs often
 * carry no parameter names, so the consumer names them.
 *
 * @param name input id, a lower-case segment such as {@code x}
 * @param type the parameter's class, used to select the constructor or method
 */
public record Param(String name, Class<?> type) {
    public Param {
        Objects.requireNonNull(type, "type");
        if (name == null || !name.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid parameter id '%s'".formatted(name));
        }
    }

    /**
     * @param name input id
     * @param type the parameter's class
     * @return the parameter
     */
    public static Param of(String name, Class<?> type) {
        return new Param(name, type);
    }
}
