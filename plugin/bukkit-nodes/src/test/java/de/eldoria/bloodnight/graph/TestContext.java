/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.internal.MobContext;
import de.eldoria.bloodnight.api.internal.MovementPreset;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Mob context for tests: the mob, no extension, no items; one thread owns everything, so work runs right away. The
 * movements the behaviour switched to are kept in {@code movements}.
 */
record TestContext(LivingEntity entity, List<MovementPreset> movements) implements MobContext {
    TestContext(LivingEntity entity) {
        this(entity, new ArrayList<>());
    }

    @Override
    public void movement(MovementPreset preset) {
        movements.add(preset);
    }

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

    @Override
    public void later(Entity entity, long ticks, Runnable work, Runnable retired) {
        work.run();
    }

    @Override
    public long effect(String id, Object source, Object target, Player viewer) {
        System.out.println("effect " + id);
        return 0;
    }

    @Override
    public void reveal(Entity entity, Player player) {}
}
