/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A node graph as stored and exchanged with the editor.
 *
 * @param nodes    nodes by instance id
 * @param comments the editor's comment frames by id; left out of the JSON when there are none
 */
public record Graph(
        Map<String, NodeInstance> nodes,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, Comment> comments) {
    public Graph {
        nodes = nodes == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(nodes));
        nodes.keySet().forEach(Graph::checkId);
        comments = comments == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(comments));
        comments.keySet().forEach(Graph::checkId);
    }

    /**
     * @param nodes nodes by instance id
     */
    public Graph(Map<String, NodeInstance> nodes) {
        this(nodes, Map.of());
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
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Id must not be blank");
    }

    /**
     * Builder for {@link Graph}.
     */
    public static final class Builder {
        private final Map<String, NodeInstance> nodes = new TreeMap<>();
        private final Map<String, Comment> comments = new TreeMap<>();

        private Builder() {}

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
         * Adds a comment frame.
         *
         * @param id      comment id, unique among the graph's comments
         * @param comment the comment
         * @return this builder
         */
        public Builder comment(String id, Comment comment) {
            if (comments.putIfAbsent(id, comment) != null)
                throw new IllegalArgumentException("Duplicate comment id " + id);
            return this;
        }

        /**
         * @return the graph
         */
        public Graph build() {
            return new Graph(nodes, comments);
        }
    }
}
