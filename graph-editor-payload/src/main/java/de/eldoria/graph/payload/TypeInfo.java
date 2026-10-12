/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A concrete type used by some port.
 *
 * @param id           canonical id, e.g. {@code java.util.List<java.lang.String>}
 * @param name         editor name, e.g. {@code List<Text>}
 * @param raw          class name without arguments
 * @param arguments    ids of the type arguments
 * @param literal      whether values of the type can be typed in
 * @param values       allowed typed-in values, e.g. enum constants or registry keys; empty when any value is fine
 * @param assignableTo ids of the other types that accept a value of this type
 * @param format       how values are typed in when not as text or a choice, e.g. {@code color}; absent otherwise
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record TypeInfo(
        String id,
        String name,
        String raw,
        List<String> arguments,
        boolean literal,
        List<String> values,
        List<String> assignableTo,
        @Nullable String format) {}
