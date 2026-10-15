/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.enchantments.BloodEnchantments;
import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The blood night reward: the loot table {@value #TABLE} of the plugin's data pack, which server owners replace with a
 * data pack of their own.
 */
public final class RewardLoot {
    /**
     * Key of the reward loot table.
     */
    public static final String TABLE = "bloodnight:blood_night/reward";

    private static final NamespacedKey KEY = new NamespacedKey("bloodnight", "blood_night/reward");

    private RewardLoot() {}

    /**
     * Rolls the reward once, or twice for a killer wearing the Curse of the Blood Moon.
     *
     * @param mob    the mob the reward is for
     * @param killer the player who killed it
     * @return the rolls of the reward table together
     */
    public static Collection<ItemStack> rollFor(LivingEntity mob, Player killer) {
        if (!BloodEnchantments.wears(killer, BloodEnchantments.BLOOD_MOON_CURSE)) return roll(mob, killer);
        List<ItemStack> loot = new ArrayList<>(roll(mob, killer));
        loot.addAll(roll(mob, killer));
        return loot;
    }

    /**
     * @param mob    the mob the reward is for
     * @param killer the player who killed it
     * @return one roll of the reward table, nothing when the table is missing or does not fit a mob's loot
     */
    public static Collection<ItemStack> roll(LivingEntity mob, Player killer) {
        LootTable table = Bukkit.getLootTable(KEY);
        if (table == null) return List.of();
        LootContext context = new LootContext.Builder(mob.getLocation())
                .lootedEntity(mob)
                .killer(killer)
                .build();
        return BukkitRuntime.rollLoot(table, context);
    }
}
