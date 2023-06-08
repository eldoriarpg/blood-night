package de.eldoria.bloodnight.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.base.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A container holding a bunch of nodes.
 */
public final class NodeContainer {
    @JsonProperty
    private final Map<Integer, Node> nodes;

    public NodeContainer() {
        this.nodes = new HashMap<>();
    }

    @JsonCreator
    public NodeContainer(@JsonProperty("nodes") Map<Integer, Node> nodes) {
        this.nodes = nodes;
    }

    public <T extends Node> T add(int id, T node) {
        if (nodes.containsKey(id)) throw new IllegalArgumentException("A node with id %s already exists!".formatted(id));
        nodes.put(id, node);
        return node;
    }

    public Node get(int id) {
        return Objects.requireNonNull(nodes.get(id));
    }
}
