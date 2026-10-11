package de.eldoria.bloodnight.mob.serialization.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import de.eldoria.bloodnight.mob.serialization.NamedAttribute;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseAttributeDeserializer<T> extends ValueDeserializer<T> {
    private final Map<String, T> mappings;

    public BaseAttributeDeserializer() {
        this.mappings = new HashMap<>();
        for (T value : values()) {
            mappings.put(id(value), value);
        }
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) {
        return map(ctxt.readValue(p, NamedAttribute.class).id());
    }

    protected abstract String id(T value);

    protected abstract T[] values();


    protected T map(String hash) {
        if (mappings.containsKey(hash)) return mappings.get(hash);
        for (T value : values()) {
            if (id(value).equals(hash)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No enchantment found for %s".formatted(hash));
    }

}
