/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.MobContext;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Mob context for tests: the mob, no extension, no items; one thread owns everything, so work runs right away.
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

    @Override
    public boolean owns(Location location) {
        return true;
    }

    @Override
    public boolean owns(Entity entity) {
        return true;
    }

    @Override
    public void at(Location location, Runnable work) {
        work.run();
    }

    @Override
    public void global(Runnable work) {
        work.run();
    }
}
