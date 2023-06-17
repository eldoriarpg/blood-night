package de.eldoria.bloodnight.mob.serialization.wrapper;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eldoria.bloodnight.mob.serialization.deserializer.EnchantmentHashSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ItemStackWrapper(Material type, @JsonSerialize(keyUsing = EnchantmentHashSerializer.class) Map<Enchantment, Integer> enchantments, String displayName,
                               List<String> lore) {
    public static ItemStackWrapper of(ItemStack stack) {
        Material type = stack.getType();
        Map<Enchantment, Integer> enchantments = stack.getEnchantments();
        ItemMeta meta = stack.getItemMeta();
        String displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        List<String> lore = meta.hasLore() ? meta.getLore() : Collections.emptyList();
        return new ItemStackWrapper(type, enchantments, displayName, lore);
    }
}
