package de.eldoria.bloodnight.mobs.serialization.serializer;

import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
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
