/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * One node placed in a graph. Pure data: what the node does is defined by the node definition registered under
 * {@link #type()}.
 *
 * @param type       stable id of the node definition, e.g. {@code math.add}
 * @param config     literal values for the definition's config fields, still unchecked
 * @param inputs     data edges by input port id
 * @param literals   values typed directly into inputs instead of connecting an edge, by input port id, still unchecked
 * @param executions chained node ids by execution port id, in execution order
 * @param state      the state of the graph the node belongs to, or {@code null} for the part that always runs
 * @param editor     editor state such as position or colour, or {@code null}; never read by the framework
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record NodeInstance(
        String type,
        Map<String, JsonNode> config,
        Map<String, Edge> inputs,
        Map<String, JsonNode> literals,
        Map<String, List<String>> executions,
        @Nullable String state,
        @Nullable JsonNode editor) {
    public NodeInstance {
        Objects.requireNonNull(type, "type");
        config = sorted(config);
        inputs = sorted(inputs);
        literals = sorted(literals);
        executions = sortedLists(executions);
        if (state != null && state.isBlank()) state = null;
        editor = editor == null || editor.isNull() || editor.isMissingNode() ? null : editor;
    }

    /**
     * Starts a node of the given type.
     *
     * @param type stable id of the node definition
     * @return a builder
     */
    public static Builder of(String type) {
        return new Builder(type);
    }

    private static <V> Map<String, V> sorted(Map<String, V> map) {
        return map == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(map));
    }

    private static Map<String, List<String>> sortedLists(Map<String, List<String>> map) {
        if (map == null) return Map.of();
        TreeMap<String, List<String>> copy = new TreeMap<>();
        map.forEach((port, targets) -> copy.put(port, List.copyOf(targets)));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Builder for {@link NodeInstance}.
     */
    public static final class Builder {
        private final String type;
        private final Map<String, JsonNode> config = new TreeMap<>();
        private final Map<String, Edge> inputs = new TreeMap<>();
        private final Map<String, JsonNode> literals = new TreeMap<>();
        private final Map<String, List<String>> executions = new TreeMap<>();
        private @Nullable String state;
        private @Nullable JsonNode editor;

        private Builder(String type) {
            this.type = type;
        }

        /**
         * Sets a config value.
         *
         * @param field config field id
         * @param value literal value
         * @return this builder
         */
        public Builder config(String field, JsonNode value) {
            config.put(field, value);
            return this;
        }

        /**
         * Connects an input to another node's output.
         *
         * @param port input port id
         * @param node id of the providing node
         * @param from output port id on the providing node
         * @return this builder
         */
        public Builder input(String port, String node, String from) {
            inputs.put(port, Edge.to(node, from));
            return this;
        }

        /**
         * Sets an input to a value instead of connecting it.
         *
         * @param port  input port id
         * @param value literal value
         * @return this builder
         */
        public Builder literal(String port, JsonNode value) {
            literals.put(port, value);
            return this;
        }

        /**
         * Appends nodes to an execution port.
         *
         * @param port  execution port id
         * @param nodes ids of the chained nodes
         * @return this builder
         */
        public Builder chain(String port, String... nodes) {
            executions.computeIfAbsent(port, key -> new ArrayList<>()).addAll(List.of(nodes));
            return this;
        }

        /**
         * Puts the node into a state of the graph.
         *
         * @param state the state's name
         * @return this builder
         */
        public Builder state(@Nullable String state) {
            this.state = state;
            return this;
        }

        /**
         * Sets the editor state.
         *
         * @param editor editor state
         * @return this builder
         */
        public Builder editor(JsonNode editor) {
            this.editor = editor;
            return this;
        }

        /**
         * @return the node
         */
        public NodeInstance build() {
            return new NodeInstance(type, config, inputs, literals, executions, state, editor);
        }
    }
}
