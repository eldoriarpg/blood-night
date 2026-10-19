/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living.spawn;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.spawn.SpawnContext;
import org.bukkit.entity.EntityType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;

/**
 * Finds the custom mobs fitting a spawn and picks one of them by weight.
 * <p>
 * Only the mobs closest to the spawning type compete: a husk takes husk mobs, and zombie mobs only when there is
 * none. Closeness is the number of steps up the entity class hierarchy from the spawning type to the mob's
 * type, counted once per pair of types.
 */
final class MobSelector {
    private static final Map<EntityType, Map<EntityType, Integer>> DISTANCES = new ConcurrentHashMap<>();

    private MobSelector() {}

    /**
     * @param mobs   every custom mob
     * @param type   the spawning entity's type
     * @param spawn  the spawn
     * @param weight a mob's weight in the spawn's world
     * @return the mobs whose type matches, whose rules allow the spawn and whose weight is above 0, of those only the
     *     ones whose type is closest to the spawning type
     */
    static List<CustomMob> candidates(
            Collection<CustomMob> mobs, EntityType type, SpawnContext spawn, ToIntFunction<CustomMob> weight) {
        List<CustomMob> fitting = mobs.stream()
                .filter(mob -> mob.attributes().isAssignable(type))
                .filter(mob -> weight.applyAsInt(mob) > 0)
                .filter(mob -> mob.spawn().allows(spawn))
                .toList();
        int closest = fitting.stream()
                .mapToInt(mob -> distance(type, mob.attributes().entityType()))
                .min()
                .orElse(0);
        return fitting.stream()
                .filter(mob -> distance(type, mob.attributes().entityType()) == closest)
                .toList();
    }

    /**
     * @return the steps from the spawning type's class up to the mob type's class, 0 for the same type
     */
    static int distance(EntityType spawning, EntityType mob) {
        if (spawning == mob) return 0;
        return DISTANCES
                .computeIfAbsent(spawning, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(mob, ignored -> steps(spawning.getEntityClass(), mob.getEntityClass()));
    }

    private static int steps(@Nullable Class<?> from, @Nullable Class<?> to) {
        if (from == null || to == null) return Integer.MAX_VALUE;
        Map<Class<?>, Integer> seen = new HashMap<>(Map.of(from, 0));
        Deque<Class<?>> queue = new ArrayDeque<>(List.of(from));
        while (!queue.isEmpty()) {
            Class<?> current = queue.removeFirst();
            int depth = seen.get(current);
            if (current == to) return depth;
            List<Class<?>> parents = new ArrayList<>(List.of(current.getInterfaces()));
            if (current.getSuperclass() != null) parents.add(current.getSuperclass());
            for (Class<?> parent : parents) {
                if (seen.putIfAbsent(parent, depth + 1) == null) queue.addLast(parent);
            }
        }
        return Integer.MAX_VALUE;
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
