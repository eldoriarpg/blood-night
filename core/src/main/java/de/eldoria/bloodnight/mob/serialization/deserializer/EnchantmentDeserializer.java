package de.eldoria.bloodnight.mob.serialization.deserializer;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentDeserializer extends BaseAttributeDeserializer<Enchantment> {

    @Override
    protected String id(Enchantment value) {
        return Integer.toHexString(value.getKey().hashCode());
    }

    @Override
    protected Enchantment[] values() {
        return Enchantment.values();
    }
}
