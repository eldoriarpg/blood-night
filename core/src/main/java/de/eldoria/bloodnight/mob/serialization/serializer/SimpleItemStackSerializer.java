package de.eldoria.bloodnight.mob.serialization.serializer;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import de.eldoria.bloodnight.mob.serialization.wrapper.ItemStackWrapper;
import org.bukkit.inventory.ItemStack;


public class SimpleItemStackSerializer extends ValueSerializer<ItemStack> {
    @Override
    public void serialize(ItemStack value, JsonGenerator gen, SerializationContext serializers) {
        gen.writePOJO(ItemStackWrapper.of(value));
    }
}
