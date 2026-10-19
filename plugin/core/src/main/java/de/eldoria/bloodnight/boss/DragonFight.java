/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.boss;

import de.eldoria.bloodnight.configuration.world.night.TimedNights;
import de.eldoria.bloodnight.living.MobCoordinator;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.function.Function;

/**
 * Makes a dragon fight a blood night: when an ender dragon appears in a world with blood nights switched on, a blood
 * night begins in its world and lasts until the dragon dies, and the dragon becomes the world's dragon mob, see
 * {@link TimedNights}. In a world with blood nights switched off the dragon stays a vanilla one. The night is begun
 * and let go on the global region, the dragon is changed on its own thread.
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
        if (!timed.dragonFight() || !nights.enabled(world)) return;
        tasks.global(() -> nights.hold(world, () -> left(dragon)));
        CustomMob mob = timed.dragon().isBlank() ? null : mobs.apply(timed.dragon());
        if (mob == null || !mob.attributes().isAssignable(dragon.getType())) return;
        tasks.entity(dragon, () -> {
            if (dragon.isValid() && !MobTags.isCustomMob(dragon)) coordinator.register(dragon, mob);
        });
    }

    /**
     * @return the dragon's share of its health, 0 once it is gone; read from any thread, at most a tick old
     */
    private static double left(EnderDragon dragon) {
        if (!dragon.isValid()) return 0;
        AttributeInstance max = dragon.getAttribute(Attribute.MAX_HEALTH);
        return max == null || max.getValue() <= 0 ? 1 : dragon.getHealth() / max.getValue();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        World world = dragon.getWorld();
        tasks.global(() -> nights.release(world));
    }
}
