package de.eldoria.bloodnight.nodes.base.io;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.util.Checks;
import de.eldoria.bloodnight.util.Numbers;

import java.util.HashMap;
import java.util.Map;

/**
 * Container holding the defined {@link Input}s on a node.
 */
public final class InputContainer {
    private final Map<String, DataType> inputFields;
    private final Map<String, Edge> edges;

    public InputContainer(Map<String, DataType> inputFields, Map<String, Edge> edges) {
        this.inputFields = inputFields;
        this.edges = edges;
    }

    /**
     * Constructs a new input container based on a map of fields.
     * <p>
     * Use {@link MetadataReader#readInputs(Class)} to create such a map from a class.
     *
     * @param inputFields input fields
     */
    public InputContainer(Map<String, DataType> inputFields) {
        this.inputFields = inputFields;
        this.edges = new HashMap<>();
    }

    /**
     * Connects an input field with the output field of another node.
     *
     * @param container node container holding the nodes.
     * @param name      name of the linked node.
     * @param edge      edge
     * @return this instance.
     */
    public InputContainer connect(NodeContainer container, String name, Edge edge) {
        // Get the type of the input field
        var type = inputFields.get(name);
        Checks.notNull(type, "Data type is null for field %s.".formatted(name));
        // Get the output type of the linked field
        DataType outputType = container.get(edge.node()).output(container).getType(edge.name());
        Checks.isSubType(outputType, type);
        // Register the edge from this container to another.
        edges.put(name, edge);
        return this;
    }

    /**
     * Get the value of an input node
     *
     * @param container node container holding nodes
     * @param name      name of the field to retrieve
     * @param <T>       required return type
     * @return value of the input field
     */
    @SuppressWarnings("unchecked")
    public <T> T value(NodeContainer container, String name) {
        if (!edges.containsKey(name)) {
            throw new IllegalStateException("No field with with name %s present. Available names are %s.".formatted(name, String.join(", ", inputFields.keySet())));
        }
        var field = edges.get(name);
        Checks.notNull(field, "No value set for field with name %s. Available names are %s.".formatted(name, String.join(", ", inputFields.keySet())));
        return (T) container.get(field.node()).output(container).value(field.name());
    }

    /**
     * Returns an {@link InputMapping} providing parsing methods for the input.
     *
     * @param container node container holding nodes
     * @param name      name of the field to retrieve
     * @return value wrapped in an input mapping
     */
    public InputMapping map(NodeContainer container, String name) {
        return new InputMapping(value(container, name));
    }

    public Map<String, Edge> edges() {
        return edges;
    }

    public record InputMapping(Object object) {
        /**
         * Get the value as integer.
         *
         * @return integer
         */
        public int asInt() {
            return Numbers.asInt(get());
        }

        /**
         * Get the value as double.
         *
         * @return double
         */
        public double asDouble() {
            return Numbers.asDouble(get());
        }

        /**
         * Get the value as float.
         *
         * @return float
         */
        public float asFloat() {
            return Numbers.asFloat(get());
        }

        /**
         * Get the value as the requested type.
         *
         * @param <T> type of value
         * @return value
         */
        @SuppressWarnings("unchecked")
        public <T> T get() {
            return (T) object;
        }
    }
}
