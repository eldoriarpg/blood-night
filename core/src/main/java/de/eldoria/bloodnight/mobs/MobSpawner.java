/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.api.event.CustomMobSpawnEvent;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.MobSpawning;
import de.eldoria.bloodnight.graph.runtime.Integrations;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Turns spawning entities into custom mobs.
 * <p>
 * One tick after an entity spawned (so other plugins can change it first), it is taken over when its world is
 * enabled, it is no custom mob or extension yet, no other plugin owns it, its spawn reason is not ignored by the world, some custom mob fits
 * the spawn by type, spawn rules and weight, and the world's spawn chance hits. The mob is picked by weight. Mobs
 * spawned during a blood night are marked with its number, so they can go when it ends.
 */
public class MobSpawner implements Listener {
    private final Supplier<? extends Collection<CustomMob>> mobs;
    private final Function<World, WorldSettings> settings;
    private final MobCoordinator coordinator;
    private final BloodNights nights;
    private final Tasks tasks;
    private final PluginManager events;
    private final ForeignMobs foreign;
    private final RandomGenerator random;

    /**
     * @param mobs          every custom mob
     * @param settings      the settings of a world
     * @param coordinator   registers the custom mobs
     * @param nights        the running blood nights
     * @param tasks         runs the takeover on the entity's thread
     * @param events        where {@link CustomMobSpawnEvent} is called, so other plugins can keep an entity as it is
     * @param foreign       tells mobs of other plugins, which stay as they are
     * @param random        rolls the spawn chance and the pick; used from several threads
     */
    public MobSpawner(
            Supplier<? extends Collection<CustomMob>> mobs,
            Function<World, WorldSettings> settings,
            MobCoordinator coordinator,
            BloodNights nights,
            Tasks tasks,
            PluginManager events,
            ForeignMobs foreign,
            RandomGenerator random) {
        this.mobs = mobs;
        this.settings = settings;
        this.coordinator = coordinator;
        this.nights = nights;
        this.tasks = tasks;
        this.events = events;
        this.foreign = foreign;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        tasks.entity(entity, () -> handleSpawnedMob(entity));
    }

    void handleSpawnedMob(LivingEntity entity) {
        if (!entity.isValid() || MobTags.isCustomMob(entity) || MobTags.isExtension(entity)) return;
        if (!foreign.mayWrap(entity)) return;
        WorldSettings worldSettings = settings.apply(entity.getWorld());
        MobSpawning spawning = worldSettings.mobSettings().spawning();
        if (!worldSettings.enabled() || spawning.ignoredReasons().contains(entity.getEntitySpawnReason())) return;
        OptionalLong night = nights.running(entity.getWorld());
        List<CustomMob> candidates = MobSelector.candidates(
                mobs.get(), entity.getType(), SpawnContexts.of(entity, night.isPresent()), spawning::weight);
        if (candidates.isEmpty() || spawning.spawnPercentage() <= random.nextInt(100)) return;
        MobSelector.pick(candidates, spawning::weight, random).ifPresent(mob -> {
            CustomMobSpawnEvent spawn = new CustomMobSpawnEvent(
                    entity,
                    mob.id(),
                    Objects.requireNonNullElse(entity.getEntitySpawnReason(), CreatureSpawnEvent.SpawnReason.DEFAULT));
            events.callEvent(spawn);
            if (spawn.isCancelled()) return;
            LivingEntity taking = mob.attributes().mythic() ? mythic(entity, mob) : entity;
            if (taking == null) return;
            night.ifPresent(number -> MobTags.markNight(taking, number));
            coordinator.register(taking, mob);
        });
    }

    /**
     * Spawns the mob's MythicMobs mob where the entity stands and removes the entity.
     *
     * @return the MythicMobs mob, or {@code null} when it could not spawn and the entity stays as it is
     */
    private static @Nullable LivingEntity mythic(LivingEntity entity, CustomMob mob) {
        Entity spawned = Integrations.spawnMythicMob(
                mob.attributes().mythicType(),
                entity.getLocation(),
                mob.attributes().mythicLevel());
        if (!(spawned instanceof LivingEntity living)) {
            if (spawned != null) spawned.remove();
            return null;
        }
        entity.remove();
        return living;
    }
}
