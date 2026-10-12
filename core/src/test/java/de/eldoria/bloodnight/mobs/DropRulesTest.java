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
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DropRulesTest {
    private static final Drop BONE = new Drop("minecraft:bone", 2, 1);
    private static final Drop GEM = new Drop("gem", 1, 3);
    private static final Drop SWORD = new Drop("minecraft:iron_sword", 1, 1);
    private static final GlobalDrops GLOBAL = new GlobalDrops(Map.of(1, 1), List.of(BONE), ValueModifier.MULTIPLY, 2);
    private static final WorldDrops WORLD = new WorldDrops(
            ConfigurationScope.WORLD,
            Map.of(2, 1),
            List.of(ConfigurationScope.GLOBAL, ConfigurationScope.WORLD),
            List.of(GEM),
            ConfigurationScope.WORLD,
            ValueModifier.VALUE,
            7);

    /**
     * Answers {@code nextInt(bound)} with the given values in order.
     */
    private static RandomGenerator scripted(int... values) {
        Deque<Integer> queue = new ArrayDeque<>();
        for (int value : values) queue.add(value);
        return new RandomGenerator() {
            @Override
            public long nextLong() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int nextInt(int bound) {
                return queue.removeFirst();
            }
        };
    }

    @Test
    void mobsWithoutDropsUseTheirWorlds() {
        DropRules rules = DropRules.of(null, WORLD, GLOBAL);

        assertEquals(Map.of(2, 1), rules.weights());
        assertEquals(List.of(BONE, GEM), rules.table());
        assertEquals(7, rules.experience(3));
    }

    @Test
    void scopesPointAtTheirLevel() {
        MobDrops mob = new MobDrops(
                ConfigurationScope.GLOBAL,
                Map.of(3, 1),
                List.of(ConfigurationScope.MOB, ConfigurationScope.WORLD),
                List.of(SWORD),
                ConfigurationScope.GLOBAL,
                ValueModifier.DEFAULT,
                0);

        DropRules rules = DropRules.of(mob, WORLD, GLOBAL);

        assertEquals(Map.of(1, 1), rules.weights());
        assertEquals(List.of(SWORD, GEM), rules.table());
        assertEquals(6, rules.experience(3));
    }

    @Test
    void theCountAndEachDropArePickedByWeight() {
        DropRules rules = new DropRules(Map.of(1, 9, 2, 1), List.of(BONE, GEM), ValueModifier.DEFAULT, 0);

        assertEquals(List.of(GEM), rules.roll(scripted(0, 1)));
        assertEquals(List.of(BONE, GEM), rules.roll(scripted(9, 0, 3)));
    }

    @Test
    void nothingDropsWithoutWeightsOrEntries() {
        assertEquals(List.of(), new DropRules(Map.of(), List.of(BONE), ValueModifier.DEFAULT, 0).roll(scripted()));
        assertEquals(List.of(), new DropRules(Map.of(1, 1), List.of(), ValueModifier.DEFAULT, 0).roll(scripted(0)));
        assertEquals(
                List.of(),
                new DropRules(Map.of(1, 1), List.of(new Drop("x", 1, 0)), ValueModifier.DEFAULT, 0).roll(scripted(0)));
    }

    @Test
    void experienceFollowsItsModifier() {
        assertEquals(5, new DropRules(Map.of(), List.of(), ValueModifier.DEFAULT, 9).experience(5));
        assertEquals(15, new DropRules(Map.of(), List.of(), ValueModifier.MULTIPLY, 3).experience(5));
        assertEquals(9, new DropRules(Map.of(), List.of(), ValueModifier.VALUE, 9).experience(5));
    }
}
