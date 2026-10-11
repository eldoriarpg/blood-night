/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.MobContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Mob context for tests: the mob, no extension, no items.
 */
record TestContext(LivingEntity entity) implements MobContext {
    @Override
    public Entity extension() {
        return null;
    }

    @Override
    public ItemStack item(String id) {
        return null;
    }
}
