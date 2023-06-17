package de.eldoria.bloodnight.mob.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.eldoria.bloodnight.mob.serialization.wrapper.ItemStackWrapper;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class SimpleItemStackSerializer extends JsonSerializer<ItemStack> {
    @Override
    public void serialize(ItemStack value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(ItemStackWrapper.of(value));
    }
}
