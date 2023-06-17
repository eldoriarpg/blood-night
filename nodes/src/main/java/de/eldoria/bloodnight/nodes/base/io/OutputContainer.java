package de.eldoria.bloodnight.nodes.base.io;

import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.MetadataReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Container holding the defined {@link Output}s on a node.
 */
public final class OutputContainer {
    private final Map<String, DataType> outputFields;
    private final Map<String, Object> values;

    /**
     * Constructs a new output container based on a map of fields.
     * <p>
     * Use {@link MetadataReader#readOutputs(Class)} to create such a map from a class.
     *
     * @param outputFields output fields
     */
    public OutputContainer(Map<String, DataType> outputFields) {
        this.outputFields = Collections.unmodifiableMap(outputFields);
        this.values = new HashMap<>(outputFields.size());
    }

    /**
     * Set the value of the output field
     *
     * @param field field to set
     * @param value value of the field
     * @return this output container
     */
    public OutputContainer set(String field, Object value) {
        if (outputFields.containsKey(field)) {
            values.put(field, value);
        }
        return this;
    }

    /**
     * Get the value of the field.
     *
     * @param name name of the field
     * @return value
     */
    public Object value(String name) {
        return values.get(name);
    }

    /**
     * Get the type of the output field
     *
     * @param name name of the field
     * @return type of the field
     */
    public DataType getType(String name) {
        return outputFields.get(name);
    }
}
