package de.eldoria.bloodnight.mob.serialization.serializer;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentSerializer extends BaseAttributeSerializer<Enchantment> {

    @Override
    public String id(Enchantment value) {
        return Integer.toHexString(value.getKey().hashCode());
    }

    @Override
    public String name(Enchantment value) {
        return value.getKey().getKey();
    }
}
