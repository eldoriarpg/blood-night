package de.eldoria.graph.definition;

import java.util.Objects;

/**
 * An execution output: a slot nodes are chained into.
 *
 * @param id   stable id, unique among the node's execution ports
 * @param name name shown in the editor
 */
public record ExecutionPort(String id, String name) {
    public ExecutionPort {
        Ids.check(id, "execution port");
        Objects.requireNonNull(name, "name");
    }
}
