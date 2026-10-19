/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.drop;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes the loot of a dying mob, leaving its equipment alone. A drop counts as equipment when the mob wears or holds
 * an item of the same kind: vanilla damages dropped equipment at random, so items are matched by material only.
 * Leaving equipment alone also keeps items a mob picked up from being multiplied.
 */
final class Loot {
    private Loot() {}

    /**
     * Multiplies the amount of every loot drop, keeping each stack within its size.
     */
    static void multiply(LivingEntity entity, List<ItemStack> drops, double factor) {
        List<Material> equipment = equipment(entity);
        for (ItemStack drop : drops) {
            if (equipment.remove(drop.getType())) continue;
            int amount = (int) Math.round(drop.getAmount() * factor);
            drop.setAmount(Math.clamp(amount, 1, drop.getMaxStackSize()));
        }
    }

    /**
     * Removes every loot drop, keeping the equipment.
     */
    static void clear(LivingEntity entity, List<ItemStack> drops) {
        List<Material> equipment = equipment(entity);
        drops.removeIf(drop -> !equipment.remove(drop.getType()));
    }

    private static List<Material> equipment(LivingEntity entity) {
        List<Material> materials = new ArrayList<>();
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return materials;
        List<ItemStack> items = new ArrayList<>(List.of(equipment.getArmorContents()));
        items.add(equipment.getItemInMainHand());
        items.add(equipment.getItemInOffHand());
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty()) materials.add(item.getType());
        }
        return materials;
    }
}
