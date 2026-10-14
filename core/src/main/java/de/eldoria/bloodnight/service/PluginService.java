/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.service;

import de.eldoria.bloodnight.api.BloodNightService;
import de.eldoria.bloodnight.api.NightForecast;
import de.eldoria.bloodnight.api.RunningNight;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightTimes;
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

    /**
     * @param server      the server
     * @param nights      the running blood nights
     * @param selector    decides the nights
     * @param coordinator the living mobs
     * @param mobs        a mob by id, or {@code null}
     * @param allMobs     every mob
     */
    public PluginService(
            Server server,
            BloodNights nights,
            NightSelector selector,
            MobCoordinator coordinator,
            Function<String, CustomMob> mobs,
            Supplier<? extends Collection<CustomMob>> allMobs) {
        this.server = server;
        this.nights = nights;
        this.selector = selector;
        this.coordinator = coordinator;
        this.mobs = mobs;
        this.allMobs = allMobs;
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
        return selector.next(world, horizon)
                .map(prediction -> new NightForecast(prediction.night(), prediction.ticksUntil(), prediction.forced()));
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
