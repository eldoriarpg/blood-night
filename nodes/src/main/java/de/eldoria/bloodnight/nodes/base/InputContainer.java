package de.eldoria.bloodnight.nodes.base;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.util.Checks;

import java.util.HashMap;
import java.util.Map;

public class InputContainer {
    private final Map<String, DataType> inputFields;
    private final Map<String, Field> fields = new HashMap<>();

    public InputContainer(Map<String, DataType> inputFields) {
        this.inputFields = inputFields;
    }

    public InputContainer set(NodeContainer container, String name, Field field) {
        var type = inputFields.get(name);
        Checks.notNull(type, "Data type is null for field %s.".formatted(name));
        DataType outputType = container.get(field.node()).output(container).getType(field.name());
        Checks.isSubType(outputType, type);
        fields.put(name, field);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(NodeContainer container, String name) {
        if (!fields.containsKey(name)) {
            throw new IllegalStateException("No field with with name %s present. Available names are %s.".formatted(name, String.join(", ", inputFields.keySet())));
        }
        var field = fields.get(name);
        Checks.notNull(field, "No value set for field with name %s. Available names are %s.".formatted(name, String.join(", ", inputFields.keySet())));
        return (T) container.get(field.node()).output(container).get(field.name());
    }
}
