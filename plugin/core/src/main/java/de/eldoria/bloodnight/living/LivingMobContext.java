/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living;

import de.eldoria.bloodnight.api.internal.MobContext;
import de.eldoria.bloodnight.api.internal.MovementPreset;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Context of a living custom mob, handed to its compiled behaviour.
 *
 * @param entity    the mob
 * @param extension its extension, if any
 * @param items     the current item registry
 * @param tasks     runs work on the owning thread
 * @param movement  switches the mob's movement, keeping its numbers
 */
record LivingMobContext(
        LivingEntity entity,
        @Nullable Entity extension,
        Supplier<ItemRegistry> items,
        Tasks tasks,
        Consumer<MovementPreset> movement)
        implements MobContext {
    @Override
    public void movement(MovementPreset preset) {
        movement.accept(preset);
    }

    @Override
    @Nullable
    public ItemStack item(String id) {
        return items.get().item(id);
    }

    @Override
    public boolean owns(Location location) {
        return Bukkit.isOwnedByCurrentRegion(location);
    }

    @Override
    public boolean owns(Entity entity) {
        return Bukkit.isOwnedByCurrentRegion(entity);
    }

    @Override
    public void at(Location location, Runnable work) {
        tasks.at(location, work);
    }

    @Override
    public void global(Runnable work) {
        tasks.globalNow(work);
    }

    @Override
    public void later(Entity entity, long ticks, Runnable work, Runnable retired) {
        tasks.entityLater(entity, ticks, work, retired);
    }

    @Override
    public void reveal(Entity entity, Player player) {
        tasks.entityNow(player, () -> player.showEntity(tasks.plugin(), entity));
    }
}
