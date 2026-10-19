/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.enchantments;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The enchantments of the plugin's data pack. They are data only, kept out of enchanting tables, trades and random
 * loot; the blood night reward loot table hands them out, and the plugin gives them their effects.
 */
public final class BloodEnchantments {
    /**
     * Armor enchantment, levels I to V: less damage from monsters during blood nights, and a calmer heartbeat.
     */
    public static final NamespacedKey NIGHT_WARD = new NamespacedKey("bloodnight", "night_ward");

    /**
     * Weapon enchantment, levels I to III: heals its wielder for every kill during a blood night, more per level.
     */
    public static final NamespacedKey BLOODTHIRST = new NamespacedKey("bloodnight", "bloodthirst");

    /**
     * Boots enchantment, levels I to III: walks faster during blood nights.
     */
    public static final NamespacedKey BLOOD_WALKER = new NamespacedKey("bloodnight", "blood_walker");

    /**
     * Helmet enchantment: night vision during blood nights.
     */
    public static final NamespacedKey BLOOD_VISION = new NamespacedKey("bloodnight", "blood_vision");

    /**
     * Armor enchantment, levels I to IV: protects from {@code bloodnight:blood_curse} damage; data only, it works
     * through the game's own protection.
     */
    public static final NamespacedKey BLOOD_BANE = new NamespacedKey("bloodnight", "blood_bane");

    /**
     * Weapon enchantment, levels I to V: deals more damage to special mobs.
     */
    public static final NamespacedKey DAWNBRINGER = new NamespacedKey("bloodnight", "dawnbringer");

    /**
     * Chestplate enchantment: no heartbeat, darkness pulses or scares during blood nights.
     */
    public static final NamespacedKey STEADY_HEART = new NamespacedKey("bloodnight", "steady_heart");

    /**
     * Bow and crossbow enchantment, levels I to III: makes what its arrows hit glow during blood nights.
     */
    public static final NamespacedKey HUNTERS_MARK = new NamespacedKey("bloodnight", "hunters_mark");

    /**
     * Armor curse: monsters near the wearer go for them during blood nights, and their kills roll the reward twice.
     */
    public static final NamespacedKey BLOOD_MOON_CURSE = new NamespacedKey("bloodnight", "blood_moon_curse");

    /**
     * Boots enchantment: special mobs cannot teleport or carry the wearer away, e.g. into the sky.
     */
    public static final NamespacedKey EARTH_BOUND = new NamespacedKey("bloodnight", "earth_bound");

    /**
     * Helmet enchantment: the wearer is never blinded.
     */
    public static final NamespacedKey ARCANE_VISION = new NamespacedKey("bloodnight", "arcane_vision");

    /**
     * The slots armor is worn in.
     */
    static final List<EquipmentSlot> ARMOR =
            List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private BloodEnchantments() {}

    /**
     * @return the item's level of the enchantment, 0 when it has none or the data pack is missing
     */
    public static int level(ItemStack item, NamespacedKey enchantment) {
        if (item.isEmpty()) return 0;
        Enchantment registered = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(enchantment);
        return registered == null ? 0 : item.getEnchantmentLevel(registered);
    }

    /**
     * @return whether any piece of the player's armor has the enchantment
     */
    public static boolean wears(Player player, NamespacedKey enchantment) {
        for (EquipmentSlot slot : ARMOR) {
            if (level(player.getInventory().getItem(slot), enchantment) > 0) return true;
        }
        return false;
    }
}
