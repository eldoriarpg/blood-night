package de.eldoria.bloodnight.nodes.base.io;

import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.MetadataReader;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
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
    private NodeContainer container;

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
     * <p>
     * This can be seen as a unidirectional edge between the output field of the node and the input field of this node.
     * <p>
     * The input field requires the same type set in {@link Output} as this field has set in {@link Input}.
     * <p>
     * The node also needs to exist already. You might want to define your chain backwards.
     *
     * @param name name of the input field that should be linked
     * @param edge the edge pointing at the linked node
     * @return this instance for chaining
     */
    public InputContainer connect(String name, Edge edge) {
        // Get the type of the input field
        var type = inputFields.get(name);
        Checks.notNull(type, "Data type is null for field %s.".formatted(name));
        // Get the output type of the linked field
        DataType outputType = container.get(edge.node()).output().getType(edge.name());
        Checks.isSubType(outputType, type);
        // Register the edge from this container to another.
        edges.put(name, edge);
        return this;
    }

    /**
     * Get the value of an input node
     *
     * @param <T>  required return type
     * @param name name of the field to retrieve
     * @return value of the input field
     */
    @SuppressWarnings("unchecked")
    public <T> T value(String name) {
        if (!edges.containsKey(name)) {
            throw new IllegalStateException("No field with with name %s present. Available names are %s.".formatted(name, String.join(", ", inputFields.keySet())));
        }
        var field = edges.get(name);
        Checks.notNull(field, "No value set for field with name %s. Available names are %s.".formatted(name, String.join(", ", inputFields.keySet())));
        return (T) container.get(field.node()).output().value(field.name());
    }

    /**
     * Returns an {@link InputMapping} providing parsing methods for the input.
     *
     * @param name name of the field to retrieve
     * @return value wrapped in an input mapping
     */
    public InputMapping map(String name) {
        return new InputMapping(value(name));
    }

    public Map<String, Edge> edges() {
        return edges;
    }

    public void inject(NodeContainer container) {
        this.container = container;
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
