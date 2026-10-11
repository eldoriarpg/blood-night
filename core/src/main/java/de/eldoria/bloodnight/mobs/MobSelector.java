/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.SpawnContext;
import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;

/**
 * Finds the custom mobs fitting a spawn and picks one of them by weight.
 */
final class MobSelector {
    private MobSelector() {}

    /**
     * @param mobs   every custom mob
     * @param type   the spawning entity's type
     * @param spawn  the spawn
     * @param weight a mob's weight in the spawn's world
     * @return the mobs whose type matches, whose rules allow the spawn and whose weight is above 0
     */
    static List<CustomMob> candidates(
            Collection<CustomMob> mobs, EntityType type, SpawnContext spawn, ToIntFunction<CustomMob> weight) {
        return mobs.stream()
                .filter(mob -> mob.attributes().isAssignable(type))
                .filter(mob -> weight.applyAsInt(mob) > 0)
                .filter(mob -> mob.spawn().allows(spawn))
                .toList();
    }

    /**
     * @param candidates mobs with a weight above 0
     * @param weight     a mob's weight
     * @param random     the roll
     * @return a mob, each with a chance of its weight over the sum of all weights; empty without candidates
     */
    static Optional<CustomMob> pick(
            List<CustomMob> candidates, ToIntFunction<CustomMob> weight, RandomGenerator random) {
        long total = candidates.stream().mapToLong(weight::applyAsInt).sum();
        if (total <= 0) return Optional.empty();
        long roll = random.nextLong(total);
        for (CustomMob mob : candidates) {
            roll -= weight.applyAsInt(mob);
            if (roll < 0) return Optional.of(mob);
        }
        return Optional.empty();
    }
}
