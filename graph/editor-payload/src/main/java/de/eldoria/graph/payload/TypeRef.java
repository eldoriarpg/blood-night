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
 * The type of a port as the editor sees it. Exactly one form is used:
 * <ul>
 *     <li>{@code id}: a concrete type, described in {@link Catalog#types()};</li>
 *     <li>{@code variable}: a type variable, bound per node by what is connected; {@code numeric} when only numbers
 *     bind to it;</li>
 *     <li>{@code raw} with {@code arguments}: a generic type whose arguments contain variables.</li>
 * </ul>
 *
 * @param id        id of a concrete type
 * @param variable  name of a type variable
 * @param raw       class name of a generic type containing variables
 * @param arguments its type arguments
 * @param numeric   whether the variable takes numbers only
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TypeRef(
        @Nullable String id,
        @Nullable String variable,
        @Nullable String raw,
        List<TypeRef> arguments,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean numeric) {
    /**
     * A type that is not a numeric variable.
     */
    public TypeRef(@Nullable String id, @Nullable String variable, @Nullable String raw, List<TypeRef> arguments) {
        this(id, variable, raw, arguments, false);
    }

    public TypeRef {
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
    }
}
