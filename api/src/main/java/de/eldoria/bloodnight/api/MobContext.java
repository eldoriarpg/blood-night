/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * What a compiled behaviour knows about the mob it belongs to. Passed to the constructor of every generated class.
 */
public interface MobContext {
    /**
     * @return the mob itself
     */
    LivingEntity entity();

    /**
     * @return the entity riding or carrying the mob, if it has one
     */
    @Nullable
    Entity extension();

    /**
     * @param id id of an item in the plugin's item registry
     * @return a copy of the item, or {@code null} when no item has that id
     */
    @Nullable
    ItemStack item(String id);
}
