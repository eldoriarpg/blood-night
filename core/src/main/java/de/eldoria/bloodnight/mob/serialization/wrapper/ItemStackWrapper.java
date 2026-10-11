/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.serialization.wrapper;

import de.eldoria.bloodnight.mob.serialization.deserializer.EnchantmentHashSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ItemStackWrapper(
        Material type,

        @JsonSerialize(keyUsing = EnchantmentHashSerializer.class)
        Map<Enchantment, Integer> enchantments,

        @Nullable String displayName,
        List<String> lore) {
    public static ItemStackWrapper of(ItemStack stack) {
        Material type = stack.getType();
        Map<Enchantment, Integer> enchantments = stack.getEnchantments();
        ItemMeta meta = stack.getItemMeta();
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : null;
        List<String> lore = meta != null && meta.hasLore() ? meta.getLore() : null;
        if (lore == null) lore = Collections.emptyList();
        return new ItemStackWrapper(type, enchantments, displayName, lore);
    }
}
