/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect.member;

import java.util.Objects;

/**
 * A parameter of a constructor or factory exposed as a node, with the input id the node gives it. Compiled APIs often
 * carry no parameter names, so the consumer names them.
 *
 * @param name        input id, a lower-case segment such as {@code x}
 * @param type        the parameter's class, used to select the constructor or method
 * @param description what the input means, used when the API's documentation has none; empty for none
 */
public record Param(String name, Class<?> type, String description) {
    public Param {
        Objects.requireNonNull(description, "description");
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
        return new Param(name, type, "");
    }

    /**
     * @param name        input id
     * @param type        the parameter's class
     * @param description what the input means, when the API's documentation does not say
     * @return the parameter
     */
    public static Param of(String name, Class<?> type, String description) {
        return new Param(name, type, description);
    }
}
