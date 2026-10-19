/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.TypeRef;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * A value set on the node in the editor.
 *
 * @param id           stable field id
 * @param name         editor name
 * @param type         the field's type; has a literal form, or is a variable a type field of the node picks
 * @param defaultValue value used when none is set, absent for required fields
 * @param selects      for a type field: the type variable of the node it binds; ports and fields typed by that
 *                     variable take the picked type
 * @param choices      for a type field: the selectable type ids it offers, keys of {@link Catalog#selectable()}
 * @param beside       the input or execution port the field is drawn beside, e.g. a switch case's value beside the
 *                     case; absent for fields drawn with the node's other settings
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfigInfo(
        String id,
        String name,
        TypeRef type,
        @Nullable JsonNode defaultValue,
        @Nullable String selects,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<String> choices,
        @Nullable String beside) {
    /**
     * A plain value field.
     */
    public ConfigInfo(String id, String name, TypeRef type, @Nullable JsonNode defaultValue) {
        this(id, name, type, defaultValue, null, List.of(), null);
    }

    public ConfigInfo {
        choices = choices == null ? List.of() : List.copyOf(choices);
    }
}
