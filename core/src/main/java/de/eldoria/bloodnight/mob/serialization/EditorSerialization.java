package de.eldoria.bloodnight.mob.serialization;

import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleSerializers;
import de.eldoria.bloodnight.mob.serialization.deserializer.EnchantmentDeserializer;
import de.eldoria.bloodnight.mob.serialization.deserializer.PotionEffectTypeDeserializer;
import de.eldoria.bloodnight.mob.serialization.serializer.EnchantmentSerializer;
import de.eldoria.bloodnight.mob.serialization.serializer.PotionEffectTypeSerializer;
import de.eldoria.bloodnight.mob.serialization.serializer.SimpleItemStackSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class EditorSerialization extends JacksonModule {
    @Override
    public String getModuleName() {
        return "MobSerialization";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, "", "de.eldoria.bloodnight", "core");
    }

    @Override
    public void setupModule(SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(Enchantment.class, new EnchantmentSerializer());
        serializers.addSerializer(PotionEffectType.class, new PotionEffectTypeSerializer());
        // Item stacks only need to be sent but never come back.
        serializers.addSerializer(ItemStack.class, new SimpleItemStackSerializer());

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(Enchantment.class, new EnchantmentDeserializer());
        deserializers.addDeserializer(PotionEffectType.class, new PotionEffectTypeDeserializer());

        context.addSerializers(serializers);
        context.addDeserializers(deserializers);
    }
}
