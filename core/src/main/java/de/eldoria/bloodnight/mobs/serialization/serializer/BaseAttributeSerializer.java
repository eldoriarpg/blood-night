package de.eldoria.bloodnight.mobs.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.eldoria.bloodnight.mobs.serialization.NamedAttribute;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;

public abstract class BaseAttributeSerializer<T> extends JsonSerializer<T> {
        @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(new NamedAttribute(String.valueOf(id(value)), name(value)));
    }

    public abstract String id(T value);

    public abstract String name(T value);
}
