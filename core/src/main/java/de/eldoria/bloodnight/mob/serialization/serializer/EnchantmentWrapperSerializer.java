package de.eldoria.bloodnight.mobs.serialization.serializer;

import org.bukkit.enchantments.EnchantmentWrapper;

public class EnchantmentWrapperSerializer extends BaseAttributeSerializer<EnchantmentWrapper> {

    @Override
    public String id(EnchantmentWrapper value) {
        return Integer.toHexString(value.getKey().hashCode());
    }

    @Override
    public String name(EnchantmentWrapper value) {
        return value.getKey().getKey();
    }
}
