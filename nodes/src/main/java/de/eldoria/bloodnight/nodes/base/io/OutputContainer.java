package de.eldoria.bloodnight.nodes.base.io;

import de.eldoria.bloodnight.nodes.DataType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class OutputContainer {
    private final Map<String, DataType> outputFields;
    private final Map<String, Object> values;

    public OutputContainer(Map<String, DataType> outputFields) {
        this.outputFields = Collections.unmodifiableMap(outputFields);
        this.values = new HashMap<>(outputFields.size());
    }

    public OutputContainer set(String field, Object value) {
        if (outputFields.containsKey(field)) {
            values.put(field, value);
        }
        return this;
    }

    public Object get(String name) {
        return values.get(name);
    }

    public DataType getType(String name) {
        return outputFields.get(name);
    }
}
