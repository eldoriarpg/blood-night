/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.configuration.elements.world.night.TimedNights;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.function.Function;

/**
 * Makes a dragon fight a blood night: when an ender dragon appears, a blood night begins in its world and lasts until
 * the dragon dies, and the dragon becomes the world's dragon mob, see {@link TimedNights}. The night is begun and let
 * go on the global region, the dragon is changed on its own thread.
 */
public final class DragonFight implements Listener {
    private final BloodNights nights;
    private final Function<World, TimedNights> settings;
    private final Function<String, CustomMob> mobs;
    private final MobCoordinator coordinator;
    private final Tasks tasks;

    /**
     * @param nights      the running blood nights
     * @param settings    the timed nights of a world
     * @param mobs        a custom mob by id, or {@code null}
     * @param coordinator turns the dragon into its mob
     * @param tasks       runs work on the right threads
     */
    public DragonFight(
            BloodNights nights,
            Function<World, TimedNights> settings,
            Function<String, CustomMob> mobs,
            MobCoordinator coordinator,
            Tasks tasks) {
        this.nights = nights;
        this.settings = settings;
        this.mobs = mobs;
        this.coordinator = coordinator;
        this.tasks = tasks;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        World world = dragon.getWorld();
        TimedNights timed = settings.apply(world);
        if (!timed.dragonFight()) return;
        tasks.global(() -> nights.hold(world));
        CustomMob mob = timed.dragon().isBlank() ? null : mobs.apply(timed.dragon());
        if (mob == null || !mob.attributes().isAssignable(dragon.getType())) return;
        tasks.entity(dragon, () -> {
            if (dragon.isValid() && !MobTags.isCustomMob(dragon)) coordinator.register(dragon, mob);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        World world = dragon.getWorld();
        tasks.global(() -> nights.release(world));
    }
}
