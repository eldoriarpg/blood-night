package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.base.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class NodeContainer {
    private final Map<Integer, Node> nodes = new HashMap<>();

    public <T extends Node> T add(int id, T node) {
        if (nodes.containsKey(id)) throw new IllegalArgumentException("A node with id %s already exists!".formatted(id));
        nodes.put(id, node);
        return node;
    }

    public Node get(int next) {
        return Objects.requireNonNull(nodes.get(next));
    }
}
