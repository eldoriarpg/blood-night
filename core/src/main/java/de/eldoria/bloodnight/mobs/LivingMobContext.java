/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.api.MobContext;
import de.eldoria.bloodnight.items.ItemRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Context of a living custom mob, handed to its compiled behaviour.
 *
 * @param entity    the mob
 * @param extension its extension, if any
 * @param items     the current item registry
 */
record LivingMobContext(LivingEntity entity, @Nullable Entity extension, Supplier<ItemRegistry> items)
        implements MobContext {
    @Override
    @Nullable
    public ItemStack item(String id) {
        ItemStack item = items.get().registrations().get(id);
        return item == null ? null : item.clone();
    }
}
