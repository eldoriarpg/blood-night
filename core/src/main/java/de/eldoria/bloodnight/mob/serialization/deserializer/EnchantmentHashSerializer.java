package de.eldoria.bloodnight.mob.serialization.deserializer;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import org.bukkit.enchantments.Enchantment;


public class EnchantmentHashSerializer extends ValueSerializer<Enchantment> {
    @Override
    public void serialize(Enchantment value, JsonGenerator gen, SerializationContext serializers) {
        gen.writeName(Integer.toHexString(value.getKey().hashCode()));
    }
}
