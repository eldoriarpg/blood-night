/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.service;

import de.eldoria.bloodnight.api.BloodNightService;
import de.eldoria.bloodnight.api.MobInfo;
import de.eldoria.bloodnight.api.NightForecast;
import de.eldoria.bloodnight.api.RunningNight;
import de.eldoria.bloodnight.api.WorldInfo;
import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.living.MobCoordinator;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobOrigin;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The {@link BloodNightService} other plugins get, answered from the running blood nights, the night selector and the
 * living mobs.
 */
public final class PluginService implements BloodNightService {
    private final Server server;
    private final BloodNights nights;
    private final NightSelector selector;
    private final MobCoordinator coordinator;
    private final Function<String, CustomMob> mobs;
    private final Supplier<? extends Collection<CustomMob>> allMobs;
    private final Function<World, WorldSettings> worlds;

    /**
     * @param server      the server
     * @param nights      the running blood nights
     * @param selector    decides the nights
     * @param coordinator the living mobs
     * @param mobs        a mob by id, or {@code null}
     * @param allMobs     every mob
     * @param worlds      a world's settings
     */
    public PluginService(
            Server server,
            BloodNights nights,
            NightSelector selector,
            MobCoordinator coordinator,
            Function<String, CustomMob> mobs,
            Supplier<? extends Collection<CustomMob>> allMobs,
            Function<World, WorldSettings> worlds) {
        this.server = server;
        this.nights = nights;
        this.selector = selector;
        this.coordinator = coordinator;
        this.mobs = mobs;
        this.allMobs = allMobs;
        this.worlds = worlds;
    }

    @Override
    public boolean isRunning(World world) {
        return nights.running(world).isPresent();
    }

    @Override
    public Optional<RunningNight> running(World world) {
        OptionalLong night = nights.running(world);
        if (night.isEmpty()) return Optional.empty();
        NightTimes times = selector.times(world);
        long now = NightTimes.now(world);
        long end = times.endOf(night.getAsLong());
        long left = Math.max(0, end - now);
        double share = times.length() == 0 ? 0 : Math.clamp((double) left / times.length(), 0, 1);
        return Optional.of(new RunningNight(night.getAsLong(), left, share, nights.held(world)));
    }

    @Override
    public Optional<NightForecast> next(World world, int horizon) {
        long coming = selector.times(world).upcoming(NightTimes.now(world));
        return selector.next(world, horizon)
                .map(prediction -> new NightForecast(
                        prediction.night(), prediction.night() - coming, prediction.ticksUntil(), prediction.forced()));
    }

    @Override
    public int probability(World world, int nightsAhead) {
        long night = selector.times(world).upcoming(NightTimes.now(world)) + Math.max(nightsAhead, 0);
        if (selector.isForced(world, night)) return 100;
        if (selector.isCancelled(world, night)) return 0;
        return selector.probability(world, night);
    }

    @Override
    public void force(World world) {
        nights.force(world);
    }

    @Override
    public void cancel(World world) {
        nights.cancel(world);
    }

    @Override
    public Set<World> runningWorlds() {
        return server.getWorlds().stream().filter(this::isRunning).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isCustomMob(Entity entity) {
        return MobTags.isCustomMob(entity);
    }

    @Override
    public Optional<String> customMobId(Entity entity) {
        return MobTags.getCustomMobId(entity);
    }

    @Override
    public Set<String> customMobIds() {
        return allMobs.get().stream().map(CustomMob::id).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<MobInfo> mob(String mobId) {
        return Optional.ofNullable(mobs.apply(mobId)).map(PluginService::info);
    }

    @Override
    public Optional<MobInfo> mob(Entity entity) {
        return customMobId(entity).flatMap(this::mob);
    }

    @Override
    public Collection<MobInfo> mobs() {
        return allMobs.get().stream().map(PluginService::info).toList();
    }

    @Override
    public int weight(World world, String mobId) {
        CustomMob mob = mobs.apply(mobId);
        return mob == null ? 0 : worlds.apply(world).mobSettings().spawning().weight(mob);
    }

    @Override
    public boolean isEnabled(World world) {
        return worlds.apply(world).enabled();
    }

    @Override
    public WorldInfo world(World world) {
        WorldSettings settings = worlds.apply(world);
        boolean timed = world.isFixedTime();
        return new WorldInfo(
                settings.enabled(),
                timed,
                timed
                        ? OptionalLong.empty()
                        : OptionalLong.of(settings.nightSettings().begin()),
                timed
                        ? OptionalLong.empty()
                        : OptionalLong.of(settings.nightSettings().end()),
                settings.nightSelection().type().name(),
                settings.mobSettings().healthMultiplier(),
                settings.mobSettings().damageModifier());
    }

    private static MobInfo info(CustomMob mob) {
        MobOrigin origin = mob.origin();
        return new MobInfo(
                mob.id(),
                mob.attributes().name().style(),
                mob.attributes().name().texts(),
                mob.attributes().entityType(),
                Optional.ofNullable(origin).map(MobOrigin::store),
                mob.spawn().weight());
    }

    @Override
    public Optional<LivingEntity> spawn(String mobId, Location location) {
        CustomMob definition = mobs.apply(mobId);
        if (definition == null) return Optional.empty();
        Entity entity = location.getWorld()
                .spawnEntity(location, definition.attributes().entityType());
        if (!(entity instanceof LivingEntity living)) {
            entity.remove();
            return Optional.empty();
        }
        nights.running(living.getWorld()).ifPresent(night -> MobTags.markNight(living, night));
        coordinator.register(living, definition);
        return Optional.of(living);
    }
}
