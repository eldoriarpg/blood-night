package de.eldoria.graph.model;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A node graph as stored and exchanged with the editor.
 *
 * @param nodes nodes by instance id
 */
public record Graph(Map<String, NodeInstance> nodes) {
    public Graph {
        nodes = nodes == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(nodes));
        nodes.keySet().forEach(Graph::checkId);
    }

    /**
     * Starts an empty graph.
     *
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Looks up a node.
     *
     * @param id instance id
     * @return the node, if present
     */
    public Optional<NodeInstance> node(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    private static void checkId(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Node id must not be blank");
    }

    /**
     * Builder for {@link Graph}.
     */
    public static final class Builder {
        private final Map<String, NodeInstance> nodes = new TreeMap<>();

        private Builder() {
        }

        /**
         * Adds a node.
         *
         * @param id   instance id, unique within the graph
         * @param node the node
         * @return this builder
         */
        public Builder node(String id, NodeInstance node) {
            if (nodes.putIfAbsent(id, node) != null) throw new IllegalArgumentException("Duplicate node id " + id);
            return this;
        }

        /**
         * Adds a node.
         *
         * @param id   instance id, unique within the graph
         * @param node the node under construction
         * @return this builder
         */
        public Builder node(String id, NodeInstance.Builder node) {
            return node(id, node.build());
        }

        /**
         * @return the graph
         */
        public Graph build() {
            return new Graph(nodes);
        }
    }
}
