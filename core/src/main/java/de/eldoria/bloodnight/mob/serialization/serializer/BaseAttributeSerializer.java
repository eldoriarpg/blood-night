package de.eldoria.bloodnight.mob.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.eldoria.bloodnight.mob.serialization.NamedAttribute;

import java.io.IOException;

public abstract class BaseAttributeSerializer<T> extends JsonSerializer<T> {
        @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(new NamedAttribute(String.valueOf(id(value)), name(value)));
    }

    public abstract String id(T value);

    public abstract String name(T value);
}
