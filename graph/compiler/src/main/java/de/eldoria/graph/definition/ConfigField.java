/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A literal value set in the editor rather than connected by an edge.
 * <p>
 * The value is stored as JSON in the graph and turned into Java source by the {@link de.eldoria.graph.types.LiteralCodec}
 * registered for {@link #type()}; node definitions only ever see the emitted code.
 * <p>
 * A field that {@linkplain #selects() selects} a type holds the id of a selectable type, e.g. {@code entity}, and binds
 * a type variable of the node to that type before anything else is inferred, so one node serves every type: the cast
 * whose output is the picked type, or a value whose own value field is typed by it.
 *
 * @param id           stable id, unique among the node's config fields and inputs
 * @param name         name shown in the editor
 * @param type         Java type of the value; needs a literal codec, or is a variable a type field binds
 * @param defaultValue value used when the graph sets none, or {@code null} when the field is required; a JSON null
 *                     lets the field stay empty, which the node handles
 * @param selects      the type variable this field binds, or {@code null} for a plain value
 * @param choices      the selectable type ids a type field allows, in editor order; empty for a plain value
 * @param beside       the input or execution port the editor draws the field beside, e.g. the case of a switch whose
 *                     value it holds; {@code null} to draw it with the node's other settings
 */
public record ConfigField(
        String id,
        String name,
        PortType type,
        @Nullable JsonNode defaultValue,
        @Nullable String selects,
        List<String> choices,
        @Nullable String beside) {
    /**
     * A plain value.
     */
    public ConfigField(String id, String name, PortType type, @Nullable JsonNode defaultValue) {
        this(id, name, type, defaultValue, null, List.of(), null);
    }

    public ConfigField {
        Ids.check(id, "config field");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        choices = List.copyOf(choices);
        if (selects != null && choices.isEmpty())
            throw new IllegalArgumentException(id + ": a type field needs choices");
    }

    /**
     * A field picking the type a variable of the node binds to.
     *
     * @param id       stable id
     * @param name     name shown in the editor
     * @param variable the variable it binds
     * @param choices  allowed selectable type ids; the first is the default
     * @return the field
     */
    public static ConfigField selecting(String id, String name, String variable, List<String> choices) {
        if (choices.isEmpty()) throw new IllegalArgumentException(id + ": a type field needs choices");
        return new ConfigField(
                id, name, PortType.of(String.class), StringNode.valueOf(choices.getFirst()), variable, choices, null);
    }

    /**
     * @return the default value, if the field has one
     */
    public Optional<JsonNode> defaultIfAbsent() {
        return Optional.ofNullable(defaultValue);
    }
}
