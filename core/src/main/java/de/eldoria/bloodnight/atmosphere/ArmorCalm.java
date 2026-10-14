/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.EnumMap;
import java.util.Map;

/**
 * How calm a player's armor keeps them, from 0 (none) to 1 (full, intact, strong armor). Each piece has a share of
 * the calm by slot, shrinking with its remaining durability down to nothing for a worn out piece; the sum is scaled by
 * the player's armor points, so strong materials calm more than weak ones.
 */
final class ArmorCalm {
    private static final double FULL_ARMOR = 20;
    private static final Map<EquipmentSlot, Double> SHARES = Map.of(
            EquipmentSlot.HEAD, 0.15,
            EquipmentSlot.CHEST, 0.4,
            EquipmentSlot.LEGS, 0.3,
            EquipmentSlot.FEET, 0.15);

    private ArmorCalm() {}

    /**
     * @return how calm the player's armor keeps them, 0 to 1
     */
    static double of(Player player) {
        AttributeInstance armor = player.getAttribute(Attribute.ARMOR);
        EntityEquipment equipment = player.getEquipment();
        Map<EquipmentSlot, Double> durability = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SHARES.keySet()) {
            ItemStack item = equipment.getItem(slot);
            if (!item.isEmpty()) durability.put(slot, durability(item));
        }
        return of(armor == null ? 0 : armor.getValue(), durability);
    }

    /**
     * @param armorPoints the player's armor points, 20 for full diamond
     * @param durability  the remaining durability of each worn piece, 0 to 1
     * @return how calm that keeps the player, 0 to 1
     */
    static double of(double armorPoints, Map<EquipmentSlot, Double> durability) {
        double share = 0;
        for (var piece : durability.entrySet()) {
            share += SHARES.getOrDefault(piece.getKey(), 0.0) * Math.clamp(piece.getValue(), 0, 1);
        }
        return Math.clamp(armorPoints / FULL_ARMOR, 0, 1) * share;
    }

    static double durability(ItemStack item) {
        int max = item.getType().getMaxDurability();
        if (max <= 0 || !(item.getItemMeta() instanceof Damageable damageable)) return 1;
        return 1 - (double) damageable.getDamage() / max;
    }
}
