/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.boss;

import de.eldoria.bloodnight.living.MobCoordinator;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Makes a wither built during a blood night a boss: it becomes one of the world's wither mobs, picked by weight. Outside
 * blood nights, in worlds with blood nights off and with no wither mobs set, withers stay vanilla. The wither is changed
 * on its own thread a tick after it was built.
 */
public final class WitherFight implements Listener {
    private final BloodNights nights;
    private final Function<World, Map<String, Integer>> withers;
    private final Function<String, @Nullable CustomMob> mobs;
    private final MobCoordinator coordinator;
    private final Tasks tasks;

    /**
     * @param nights      the running blood nights
     * @param withers     the wither mobs of a world, by id, with their weights
     * @param mobs        a custom mob by id, or {@code null}
     * @param coordinator turns the wither into its mob
     * @param tasks       runs work on the right threads
     */
    public WitherFight(
            BloodNights nights,
            Function<World, Map<String, Integer>> withers,
            Function<String, @Nullable CustomMob> mobs,
            MobCoordinator coordinator,
            Tasks tasks) {
        this.nights = nights;
        this.withers = withers;
        this.mobs = mobs;
        this.coordinator = coordinator;
        this.tasks = tasks;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBuild(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_WITHER) return;
        if (!(event.getEntity() instanceof Wither wither)) return;
        World world = wither.getWorld();
        OptionalLong night = nights.running(world);
        if (!nights.enabled(world) || night.isEmpty()) return;
        CustomMob mob = pick(withers.apply(world));
        if (mob == null) return;
        tasks.entity(wither, () -> {
            if (!wither.isValid() || MobTags.isCustomMob(wither)) return;
            MobTags.markNight(wither, night.getAsLong());
            coordinator.register(wither, mob);
        });
    }

    /**
     * @param weights mob ids with their weights
     * @return one of the mobs that exist and can be a wither, by weight; {@code null} when there is none
     */
    @Nullable
    CustomMob pick(Map<String, Integer> weights) {
        List<CustomMob> fitting = new ArrayList<>();
        List<Integer> shares = new ArrayList<>();
        int total = 0;
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            CustomMob mob = mobs.apply(entry.getKey());
            int weight = entry.getValue() == null ? 0 : entry.getValue();
            if (mob == null || weight <= 0 || !mob.attributes().isAssignable(EntityType.WITHER)) continue;
            fitting.add(mob);
            shares.add(weight);
            total += weight;
        }
        if (total == 0) return null;
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (int index = 0; index < fitting.size(); index++) {
            roll -= shares.get(index);
            if (roll < 0) return fitting.get(index);
        }
        return null;
    }
}
