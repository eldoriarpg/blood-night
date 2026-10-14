/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.MobSpawning;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Turns spawning entities into custom mobs.
 * <p>
 * One tick after an entity spawned (so other plugins can change it first), it is taken over when its world is
 * enabled, it is no custom mob or extension yet, its spawn reason is not ignored by the world, some custom mob fits
 * the spawn by type, spawn rules and weight, and the world's spawn chance hits. The mob is picked by weight. Mobs
 * spawned during a blood night are marked with its number, so they can go when it ends.
 */
public class MobSpawner implements Listener {
    private final Supplier<? extends Collection<CustomMob>> mobs;
    private final Function<World, WorldSettings> settings;
    private final MobCoordinator coordinator;
    private final BloodNights nights;
    private final Tasks tasks;
    private final RandomGenerator random;

    /**
     * @param mobs          every custom mob
     * @param settings      the settings of a world
     * @param coordinator   registers the custom mobs
     * @param nights        the running blood nights
     * @param tasks         runs the takeover on the entity's thread
     * @param random        rolls the spawn chance and the pick; used from several threads
     */
    public MobSpawner(
            Supplier<? extends Collection<CustomMob>> mobs,
            Function<World, WorldSettings> settings,
            MobCoordinator coordinator,
            BloodNights nights,
            Tasks tasks,
            RandomGenerator random) {
        this.mobs = mobs;
        this.settings = settings;
        this.coordinator = coordinator;
        this.nights = nights;
        this.tasks = tasks;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        tasks.entity(entity, () -> handleSpawnedMob(entity));
    }

    void handleSpawnedMob(LivingEntity entity) {
        if (!entity.isValid() || MobTags.isCustomMob(entity) || MobTags.isExtension(entity)) return;
        WorldSettings worldSettings = settings.apply(entity.getWorld());
        MobSpawning spawning = worldSettings.mobSettings().spawning();
        if (!worldSettings.enabled() || spawning.ignoredReasons().contains(entity.getEntitySpawnReason())) return;
        OptionalLong night = nights.running(entity.getWorld());
        List<CustomMob> candidates = MobSelector.candidates(
                mobs.get(), entity.getType(), SpawnContexts.of(entity, night.isPresent()), spawning::weight);
        if (candidates.isEmpty() || spawning.spawnPercentage() <= random.nextInt(100)) return;
        MobSelector.pick(candidates, spawning::weight, random).ifPresent(mob -> {
            night.ifPresent(number -> MobTags.markNight(entity, number));
            coordinator.register(entity, mob);
        });
    }
}
