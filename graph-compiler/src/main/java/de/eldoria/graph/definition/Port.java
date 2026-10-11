package de.eldoria.graph.definition;

import java.util.Objects;

/**
 * A typed data input or output of a node.
 *
 * @param id       stable id, unique among the node's inputs or outputs
 * @param name     name shown in the editor
 * @param type     Java type of the value
 * @param optional whether an input may stay unconnected; always false for outputs
 */
public record Port(String id, String name, PortType type, boolean optional) {
    public Port {
        Ids.check(id, "port");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}
