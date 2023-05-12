package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.base.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NodeContainer {
    private final Map<Integer, Node> nodes = new HashMap<>();

    public void add(int id, Node node) {
        nodes.put(id, node);
    }

    public Node get(int next) {
        return Objects.requireNonNull(nodes.get(next));
    }
}
