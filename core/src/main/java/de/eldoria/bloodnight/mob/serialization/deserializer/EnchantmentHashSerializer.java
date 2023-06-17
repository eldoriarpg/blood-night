package de.eldoria.bloodnight.mob.serialization.deserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;

public class EnchantmentHashSerializer extends JsonSerializer<Enchantment> {
    @Override
    public void serialize(Enchantment value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeFieldName(Integer.toHexString(value.getKey().hashCode()));
    }
}
