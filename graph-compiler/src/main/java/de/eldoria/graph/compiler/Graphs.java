/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.Port;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.StringNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds graphs in code with every reference checked against a platform.
 * <p>
 * Node types, input, output and execution ports, config fields and typed-in values are checked the moment they are
 * named, so a typo fails where it is written instead of when the graph is compiled. {@link #build()} then runs the
 * full validation: edge types, required inputs, cycles. Node types are best named through the id constants of a
 * node set rather than as raw strings.
 */
public final class Graphs {
    private static final List<PortType> SCALARS = List.of(
            PortType.of(boolean.class), PortType.of(int.class), PortType.of(double.class), PortType.of(String.class));

    private final GraphPlatform platform;
    private final MacroLibrary macros;
    private final boolean macroBody;
    private final Map<String, NodeDefinition> extra;
    private final Map<String, NodeBuilder> nodes = new LinkedHashMap<>();

    private Graphs(GraphPlatform platform, MacroLibrary macros, boolean macroBody) {
        this.platform = platform;
        this.macros = macros;
        this.macroBody = macroBody;
        Map<String, NodeDefinition> known = new LinkedHashMap<>(macros.definitions());
        if (macroBody) MacroBoundary.definitions(platform.types()).forEach(node -> known.put(node.id(), node));
        this.extra = known;
    }

    /**
     * @param platform the node set graphs are built for
     * @return a builder for a behaviour graph
     */
    public static Graphs with(GraphPlatform platform) {
        return new Graphs(platform, MacroLibrary.empty(), false);
    }

    /**
     * @param platform the node set graphs are built for
     * @param macros   macros the graph may use
     * @return a builder for a behaviour graph
     */
    public static Graphs with(GraphPlatform platform, MacroLibrary macros) {
        return new Graphs(platform, macros, false);
    }

    /**
     * Builds the body of a macro: the boundary nodes are available, and {@link #build()} leaves the full check to
     * {@link MacroLibrary#compile}.
     *
     * @param platform the node set the macro is built for
     * @param macros   other macros the body may use
     * @return a builder for a macro body
     */
    public static Graphs macroBody(GraphPlatform platform, MacroLibrary macros) {
        return new Graphs(platform, macros, true);
    }

    /**
     * Places a node.
     *
     * @param id   instance id, unique within the graph
     * @param type node type id
     * @return the node, to connect and configure
     * @throws IllegalArgumentException when the type is unknown or the id is taken
     */
    public NodeBuilder node(String id, String type) {
        NodeDefinition definition = platform.node(type)
                .or(() -> Optional.ofNullable(extra.get(type)))
                .orElseThrow(() -> new IllegalArgumentException("Unknown node type " + type));
        if (nodes.containsKey(id)) throw new IllegalArgumentException("Duplicate node id " + id);
        NodeBuilder node = new NodeBuilder(id, definition);
        nodes.put(id, node);
        return node;
    }

    /**
     * @return the graph
     * @throws IllegalArgumentException listing every problem when the graph is not valid
     */
    public Graph build() {
        Graph.Builder graph = Graph.builder();
        nodes.forEach((id, node) -> graph.node(id, node.instance));
        Graph built = graph.build();
        if (macroBody) return built;
        try {
            MacroExpander.Expansion expansion = MacroExpander.expand(built, macros.definitions());
            new GraphValidator(platform, MacroBoundary.passDefinitions(platform.types())).validate(expansion.graph());
        } catch (GraphCompilationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return built;
    }

    /**
     * A node being placed. Every name is checked against the node's definition.
     */
    public final class NodeBuilder {
        private final String id;
        private final NodeDescriptor descriptor;
        private final NodeInstance.Builder instance;

        private NodeBuilder(String id, NodeDefinition definition) {
            this.id = id;
            this.descriptor = definition.descriptor();
            this.instance = NodeInstance.of(definition.id());
        }

        /**
         * @return the instance id, for connecting other nodes to this one
         */
        public String id() {
            return id;
        }

        /**
         * Connects an input to an output of another node. When that node is already placed, its output is checked
         * here; otherwise {@link Graphs#build()} checks it.
         */
        public NodeBuilder input(String port, String node, String output) {
            requireInput(port);
            NodeBuilder source = nodes.get(node);
            if (source != null && source.descriptor.output(output).isEmpty()) {
                throw new IllegalArgumentException(
                        "%s (%s) has no output %s".formatted(node, source.descriptor.id(), output));
            }
            instance.input(port, node, output);
            return this;
        }

        /**
         * Types a value into an input.
         *
         * @param value a number, boolean, string, enum constant or JSON node
         */
        public NodeBuilder literal(String port, Object value) {
            Port input = requireInput(port);
            boolean typed = platform.types().codec(input.type()).isPresent()
                    || SCALARS.stream().anyMatch(input.type()::accepts);
            if (!typed) {
                throw new IllegalArgumentException("%s.%s of type %s cannot be typed in"
                        .formatted(id, port, platform.types().name(input.type())));
            }
            instance.literal(port, json(value));
            return this;
        }

        /**
         * Sets a config field.
         *
         * @param value a number, boolean, string, enum constant or JSON node
         */
        public NodeBuilder config(String field, Object value) {
            if (descriptor.configField(field).isEmpty()) {
                throw new IllegalArgumentException(
                        "%s (%s) has no config field %s".formatted(id, descriptor.id(), field));
            }
            instance.config(field, json(value));
            return this;
        }

        /**
         * Appends nodes to an execution port.
         */
        public NodeBuilder chain(String port, String... targets) {
            if (descriptor.execution(port).isEmpty()) {
                throw new IllegalArgumentException(
                        "%s (%s) has no execution port %s".formatted(id, descriptor.id(), port));
            }
            instance.chain(port, targets);
            return this;
        }

        /**
         * Sets the editor state.
         */
        public NodeBuilder editor(JsonNode editor) {
            instance.editor(editor);
            return this;
        }

        private Port requireInput(String port) {
            return descriptor
                    .input(port)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "%s (%s) has no input %s".formatted(id, descriptor.id(), port)));
        }
    }

    private static JsonNode json(Object value) {
        return switch (value) {
            case JsonNode node -> node;
            case Boolean bool -> BooleanNode.valueOf(bool);
            case Integer integer -> IntNode.valueOf(integer);
            case Short number -> IntNode.valueOf(number);
            case Long number -> LongNode.valueOf(number);
            case Double number -> DoubleNode.valueOf(number);
            case Float number -> DoubleNode.valueOf(number);
            case String text -> StringNode.valueOf(text);
            case Enum<?> constant -> StringNode.valueOf(constant.name());
            default ->
                throw new IllegalArgumentException(
                        "Cannot type in a %s".formatted(value.getClass().getName()));
        };
    }
}
