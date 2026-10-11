package de.eldoria.graph.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * The type of a port as the editor sees it. Exactly one form is used:
 * <ul>
 *     <li>{@code id}: a concrete type, described in {@link Catalog#types()};</li>
 *     <li>{@code variable}: a type variable, bound per node by what is connected;</li>
 *     <li>{@code raw} with {@code arguments}: a generic type whose arguments contain variables.</li>
 * </ul>
 *
 * @param id        id of a concrete type
 * @param variable  name of a type variable
 * @param raw       class name of a generic type containing variables
 * @param arguments its type arguments
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TypeRef(String id, String variable, String raw, List<TypeRef> arguments) {
    public TypeRef {
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
    }
}
