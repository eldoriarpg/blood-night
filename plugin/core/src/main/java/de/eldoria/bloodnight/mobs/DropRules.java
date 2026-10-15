/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;
import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.world.WorldDrops;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * The drop rules of one dying custom mob, resolved from its own drop settings, else its world's, with each scope
 * pointing at the global, world or mob level.
 *
 * @param weights    weights of how many drops fall, by count
 * @param table      every entry the drops are picked from
 * @param xpModifier how the experience changes
 * @param xpValue    the number it changes by
 */
record DropRules(Map<Integer, Integer> weights, List<Drop> table, ValueModifier xpModifier, double xpValue) {
    /**
     * @param mob    the mob's drop settings, if it has any
     * @param world  its world's
     * @param global the global ones
     * @return the rules
     */
    static DropRules of(@Nullable MobDrops mob, WorldDrops world, GlobalDrops global) {
        if (mob == null) {
            return resolve(world.weightsScope(), world.dropTables(), world.xpScope(), null, world, global);
        }
        return resolve(mob.weightsScope(), mob.dropTables(), mob.xpScope(), mob, world, global);
    }

    private static DropRules resolve(
            ConfigurationScope weightsScope,
            List<ConfigurationScope> tables,
            ConfigurationScope xpScope,
            @Nullable MobDrops mob,
            WorldDrops world,
            GlobalDrops global) {
        Map<Integer, Integer> weights =
                switch (weightsScope) {
                    case GLOBAL -> global.weights();
                    case WORLD -> world.weights();
                    case MOB -> mob == null ? world.weights() : mob.weights();
                };
        List<Drop> table = new ArrayList<>();
        for (ConfigurationScope scope : tables.stream().distinct().toList()) {
            switch (scope) {
                case GLOBAL -> table.addAll(global.drops());
                case WORLD -> table.addAll(world.dropList());
                case MOB -> table.addAll(mob == null ? List.of() : mob.dropList());
            }
        }
        return switch (xpScope) {
            case GLOBAL -> new DropRules(weights, table, global.xpModifier(), global.xpValue());
            case WORLD -> new DropRules(weights, table, world.xpModifier(), world.xpValue());
            case MOB ->
                mob == null
                        ? new DropRules(weights, table, world.xpModifier(), world.xpValue())
                        : new DropRules(weights, table, mob.xpModifier(), mob.xpValue());
        };
    }

    /**
     * @param random the source of chance
     * @return the drops: how many by the count weights (counts in ascending order), each picked from the table by its
     *     weight
     */
    List<Drop> roll(RandomGenerator random) {
        Integer count = pick(
                weights.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> new Weighted<>(entry.getKey(), entry.getValue()))
                        .toList(),
                random);
        if (count == null || count <= 0) return List.of();
        List<Weighted<Drop>> entries =
                table.stream().map(drop -> new Weighted<>(drop, drop.weight())).toList();
        List<Drop> drops = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Drop drop = pick(entries, random);
            if (drop == null) break;
            drops.add(drop);
        }
        return drops;
    }

    /**
     * @param vanilla the experience the game would drop
     * @return the experience to drop
     */
    int experience(int vanilla) {
        return (int) Math.round(xpModifier.apply(vanilla, xpValue));
    }

    private record Weighted<T>(T value, int weight) {}

    private static <T> @Nullable T pick(List<Weighted<T>> entries, RandomGenerator random) {
        int total =
                entries.stream().mapToInt(entry -> Math.max(0, entry.weight())).sum();
        if (total <= 0) return null;
        int roll = random.nextInt(total);
        for (Weighted<T> entry : entries) {
            if (entry.weight() <= 0) continue;
            roll -= entry.weight();
            if (roll < 0) return entry.value();
        }
        return null;
    }
}
