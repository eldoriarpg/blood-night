/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.roll;

import de.eldoria.bloodnight.loot.LootAmount;
import de.eldoria.bloodnight.loot.LootEntry;
import de.eldoria.bloodnight.loot.LootTable;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootRollTest {
    private final RandomGenerator random = new SplittableRandom(4711);
    private final Map<String, LootTable> tables = Map.of(
            "common",
                    new LootTable(
                            "Common",
                            List.of(
                                    new LootEntry("minecraft:diamond", 1, LootAmount.exactly(1), false),
                                    new LootEntry("minecraft:bone", 5, LootAmount.exactly(2), false))),
            "boss", new LootTable("Boss", List.of(new LootEntry("minecraft:diamond", 3, LootAmount.exactly(5), false))),
            "arrows",
                    new LootTable("Arrows", List.of(new LootEntry("minecraft:arrow", 1, LootAmount.exactly(8), true))));

    private List<LootRoll.Pooled> pool(LootLevel... levels) {
        return LootRoll.pool(List.of(levels), tables::get);
    }

    @Test
    void aMoreSpecificLevelReplacesTheSameItemsWhenItSaysSo() {
        LootLevel server = new LootLevel(List.of("common"), Map.of(1, 1), false);
        List<LootRoll.Pooled> replaced = pool(new LootLevel(List.of("boss"), null, true), server);
        assertEquals(List.of("boss/minecraft:diamond", "common/minecraft:bone"), names(replaced));

        List<LootRoll.Pooled> both = pool(new LootLevel(List.of("boss"), null, false), server);
        assertEquals(
                List.of("boss/minecraft:diamond", "common/minecraft:diamond", "common/minecraft:bone"), names(both));
    }

    @Test
    void aTableAssignedTwiceCountsOnceAndUnknownTablesAreLeftOut() {
        List<LootRoll.Pooled> pool = pool(
                new LootLevel(List.of("arrows", "missing"), null, false),
                new LootLevel(List.of("arrows"), null, false));
        assertEquals(List.of("arrows/minecraft:arrow"), names(pool));
    }

    @Test
    void theMostSpecificLevelThatSetsSlotsDecidesHowMany() {
        LootLevel mob = new LootLevel(List.of(), null, false);
        LootLevel world = new LootLevel(List.of(), Map.of(2, 1), false);
        LootLevel server = new LootLevel(List.of("common"), Map.of(0, 1), false);
        assertEquals(Map.of(2, 1), LootRoll.slots(List.of(mob, world, server)));
        assertEquals(Map.of(), LootRoll.slots(List.of(mob)));
        assertTrue(LootRoll.roll(List.of(server), tables::get, random).isEmpty());
        assertEquals(
                2,
                LootRoll.roll(List.of(mob, world, server), tables::get, random).size());
    }

    @Test
    void anEntryFillsOneSlotUnlessItIsRepeatable() {
        List<LootRoll.Rolled> once =
                LootRoll.roll(List.of(new LootLevel(List.of("common"), Map.of(5, 1), false)), tables::get, random);
        assertEquals(2, once.size());
        assertEquals(
                2,
                new HashSet<>(once.stream().map(rolled -> rolled.entry().item()).toList()).size());

        List<LootRoll.Rolled> again =
                LootRoll.roll(List.of(new LootLevel(List.of("arrows"), Map.of(3, 1), false)), tables::get, random);
        assertEquals(3, again.size());
        assertTrue(again.stream().allMatch(rolled -> rolled.amount() == 8));
    }

    @Test
    void amountsComeFromTheRangeOrTheChoices() {
        LootAmount range = new LootAmount(LootAmount.AmountMode.RANGE, 2, 5, Map.of());
        LootAmount choices = new LootAmount(LootAmount.AmountMode.CHOICES, 1, 1, Map.of(10, 5, 20, 3, 25, 1));
        Set<Integer> fromRange = new HashSet<>();
        Set<Integer> fromChoices = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            fromRange.add(range.roll(random));
            fromChoices.add(choices.roll(random));
        }
        assertEquals(Set.of(2, 3, 4, 5), fromRange);
        assertEquals(Set.of(10, 20, 25), fromChoices);
        assertEquals(1, new LootAmount(LootAmount.AmountMode.RANGE, -3, -9, Map.of()).roll(random));
    }

    private static List<String> names(List<LootRoll.Pooled> pool) {
        return pool.stream()
                .map(pooled -> pooled.table() + "/" + pooled.entry().item())
                .toList();
    }
}
