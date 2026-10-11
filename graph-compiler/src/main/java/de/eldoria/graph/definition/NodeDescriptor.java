package de.eldoria.graph.definition;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Everything about a node except how it emits code: its id, editor texts and ports. The editor payload is generated
 * from descriptors.
 *
 * @param id          stable id stored in graphs, e.g. {@code math.add}
 * @param kind        how the editor groups the node
 * @param category    editor category, e.g. {@code math}
 * @param name        name shown in the editor
 * @param description help text shown in the editor
 * @param inputs      data inputs
 * @param outputs     data outputs
 * @param executions  execution outputs that nodes are chained into
 * @param config      literal values set in the editor
 */
public record NodeDescriptor(String id,
                             NodeKind kind,
                             String category,
                             String name,
                             String description,
                             List<Port> inputs,
                             List<Port> outputs,
                             List<ExecutionPort> executions,
                             List<ConfigField> config) {
    public NodeDescriptor {
        Ids.checkPath(id, "node");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
        executions = List.copyOf(executions);
        config = List.copyOf(config);
        unique(id, "input or config field", inputs.stream().map(Port::id), config.stream().map(ConfigField::id));
        unique(id, "output", outputs.stream().map(Port::id));
        unique(id, "execution port", executions.stream().map(ExecutionPort::id));
        if (outputs.stream().anyMatch(Port::optional)) throw new IllegalArgumentException(id + ": outputs cannot be optional");
    }

    /**
     * Starts a descriptor. The category defaults to the first segment of the id, the name to its last segment.
     *
     * @param id   stable id, e.g. {@code math.add}
     * @param kind how the editor groups the node
     * @return a builder
     */
    public static Builder builder(String id, NodeKind kind) {
        return new Builder(id, kind);
    }

    /**
     * @param id input port id
     * @return the input, if declared
     */
    public Optional<Port> input(String id) {
        return inputs.stream().filter(port -> port.id().equals(id)).findFirst();
    }

    /**
     * @param id output port id
     * @return the output, if declared
     */
    public Optional<Port> output(String id) {
        return outputs.stream().filter(port -> port.id().equals(id)).findFirst();
    }

    /**
     * @param id execution port id
     * @return the execution port, if declared
     */
    public Optional<ExecutionPort> execution(String id) {
        return executions.stream().filter(port -> port.id().equals(id)).findFirst();
    }

    /**
     * @param id config field id
     * @return the field, if declared
     */
    public Optional<ConfigField> configField(String id) {
        return config.stream().filter(field -> field.id().equals(id)).findFirst();
    }

    @SafeVarargs
    private static void unique(String node, String what, Stream<String>... ids) {
        Set<String> seen = new HashSet<>();
        for (var stream : ids) {
            stream.forEach(id -> {
                if (!seen.add(id)) throw new IllegalArgumentException("%s: duplicate %s id '%s'".formatted(node, what, id));
            });
        }
    }

    /**
     * Builder for {@link NodeDescriptor}.
     */
    public static final class Builder {
        private final String id;
        private final NodeKind kind;
        private final List<Port> inputs = new ArrayList<>();
        private final List<Port> outputs = new ArrayList<>();
        private final List<ExecutionPort> executions = new ArrayList<>();
        private final List<ConfigField> config = new ArrayList<>();
        private String category;
        private String name;
        private String description = "";

        private Builder(String id, NodeKind kind) {
            this.id = Ids.checkPath(id, "node");
            this.kind = kind;
            this.category = id.contains(".") ? id.substring(0, id.indexOf('.')) : id;
            this.name = Ids.humanize(id);
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Adds a required input named after its id.
         */
        public Builder input(String id, PortType type) {
            return input(id, Ids.humanize(id), type, false);
        }

        /**
         * Adds a required input named after its id.
         */
        public Builder input(String id, Class<?> type) {
            return input(id, PortType.of(type));
        }

        /**
         * Adds an input that may stay unconnected.
         */
        public Builder optionalInput(String id, PortType type) {
            return input(id, Ids.humanize(id), type, true);
        }

        /**
         * Adds an input that may stay unconnected.
         */
        public Builder optionalInput(String id, Class<?> type) {
            return optionalInput(id, PortType.of(type));
        }

        public Builder input(String id, String name, PortType type, boolean optional) {
            inputs.add(new Port(id, name, type, optional));
            return this;
        }

        /**
         * Adds an output named after its id.
         */
        public Builder output(String id, PortType type) {
            return output(id, Ids.humanize(id), type);
        }

        /**
         * Adds an output named after its id.
         */
        public Builder output(String id, Class<?> type) {
            return output(id, PortType.of(type));
        }

        public Builder output(String id, String name, PortType type) {
            outputs.add(new Port(id, name, type, false));
            return this;
        }

        /**
         * Adds an execution port named after its id.
         */
        public Builder execution(String id) {
            return execution(id, Ids.humanize(id));
        }

        public Builder execution(String id, String name) {
            executions.add(new ExecutionPort(id, name, false));
            return this;
        }

        /**
         * Adds an execution port whose chained code may run several times, named after its id.
         */
        public Builder repeatingExecution(String id) {
            executions.add(new ExecutionPort(id, Ids.humanize(id), true));
            return this;
        }

        /**
         * Adds a config field named after its id.
         *
         * @param defaultValue value used when the graph sets none, or {@code null} for a required field
         */
        public Builder config(String id, Class<?> type, JsonNode defaultValue) {
            return config(id, Ids.humanize(id), PortType.of(type), defaultValue);
        }

        public Builder config(String id, String name, PortType type, JsonNode defaultValue) {
            config.add(new ConfigField(id, name, type, defaultValue));
            return this;
        }

        /**
         * @return the descriptor
         */
        public NodeDescriptor build() {
            return new NodeDescriptor(id, kind, category, name, description, inputs, outputs, executions, config);
        }
    }
}
