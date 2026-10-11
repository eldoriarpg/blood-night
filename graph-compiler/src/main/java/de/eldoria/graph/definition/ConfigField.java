package de.eldoria.graph.definition;

import tools.jackson.databind.JsonNode;

import java.util.Objects;
import java.util.Optional;

/**
 * A literal value set in the editor rather than connected by an edge.
 * <p>
 * The value is stored as JSON in the graph and turned into Java source by the {@link de.eldoria.graph.types.LiteralCodec}
 * registered for {@link #type()}; node definitions only ever see the emitted code.
 *
 * @param id           stable id, unique among the node's config fields and inputs
 * @param name         name shown in the editor
 * @param type         Java type of the value; needs a literal codec
 * @param defaultValue value used when the graph sets none, or {@code null} when the field is required
 */
public record ConfigField(String id, String name, PortType type, JsonNode defaultValue) {
    public ConfigField {
        Ids.check(id, "config field");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }

    /**
     * @return the default value, if the field has one
     */
    public Optional<JsonNode> defaultIfAbsent() {
        return Optional.ofNullable(defaultValue);
    }
}
