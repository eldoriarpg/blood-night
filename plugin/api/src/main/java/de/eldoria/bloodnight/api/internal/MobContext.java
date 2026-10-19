/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.internal;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * What a compiled behaviour knows about the mob it belongs to. Passed to the constructor of every generated class.
 * <p>
 * A behaviour runs on the thread of its mob's region. On Folia, other regions and world-wide state belong to other
 * threads; {@link #owns}, {@link #at} and {@link #global} let generated code stay on the right one. On Paper everything
 * is owned and runs right away.
 * <p>
 * For the code BloodNight generates from mob graphs; may change in any version without notice.
 */
@ApiStatus.Internal
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

    /**
     * @return whether the current thread may touch what is at the location
     */
    boolean owns(Location location);

    /**
     * @return whether the current thread may touch the entity
     */
    boolean owns(Entity entity);

    /**
     * Runs work on the thread owning the location: right away when the current thread owns it, otherwise on that
     * region's next tick.
     */
    void at(Location location, Runnable work);

    /**
     * Runs work on the global region, which owns world-wide state such as time and weather: right away when the
     * current thread is the global one, otherwise on its next tick.
     */
    void global(Runnable work);

    /**
     * Switches how the mob moves, e.g. to flee once it is hurt. Mobs that do not find paths ignore it.
     *
     * @param preset the movement
     */
    void movement(MovementPreset preset);

    /**
     * Runs work on the entity's thread after a delay, e.g. to take away something the behaviour showed.
     *
     * @param entity  the entity whose thread runs it
     * @param ticks   the delay, at least one tick
     * @param work    what to run
     * @param retired runs instead when the entity is gone before the work could run
     */
    void later(Entity entity, long ticks, Runnable work, Runnable retired);

    /**
     * Plays a particle effect of the library once, on the mob's account: it counts against the mob's limits.
     *
     * @param id     the effect's id, e.g. {@code bloodnight:blood_spear}
     * @param source where it plays: an entity, which it follows, or a location
     * @param target the other end, an entity or a location, or {@code null}
     * @param viewer the only player who sees it, or {@code null} for everyone near
     * @return ticks until what travels in it reaches the target, 0 when nothing travels; -1 when it does not play
     */
    long effect(String id, Object source, @Nullable Object target, @Nullable Player viewer);

    /**
     * Shows an entity that is hidden by default to one player.
     *
     * @param entity the hidden entity
     * @param player who sees it
     */
    void reveal(Entity entity, Player player);
}
