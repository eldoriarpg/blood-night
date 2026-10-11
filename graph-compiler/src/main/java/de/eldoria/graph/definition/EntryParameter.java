package de.eldoria.graph.definition;

import java.util.Objects;

/**
 * A parameter of the host method behind an entry point.
 *
 * @param name parameter name in generated code
 * @param type parameter type; its raw class must match the host method
 */
public record EntryParameter(String name, PortType type) {
    public EntryParameter {
        Ids.check(name, "parameter");
        Objects.requireNonNull(type, "type");
    }
}
