/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.MobName;
import de.eldoria.bloodnight.mob.meta.SpawnContext;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.graph.model.Graph;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSelectorTest {
    private static final SpawnContext NIGHT =
            new SpawnContext("world", ignored -> true, 64, 0, true, SpawnReason.NATURAL, true);
    private final CustomMob common = Mobs.zombie("common", Graph.builder().build());
    private final CustomMob rare = Mobs.zombie("rare", Graph.builder().build());
    private final Map<String, Integer> weights = new HashMap<>(Map.of("common", 9, "rare", 1));

    private int weight(CustomMob mob) {
        return weights.get(mob.id());
    }

    @Test
    void candidatesMatchTypeRulesAndWeight() {
        assertEquals(
                List.of(common, rare),
                MobSelector.candidates(List.of(common, rare), EntityType.ZOMBIE, NIGHT, this::weight));
        assertEquals(
                List.of(common, rare),
                MobSelector.candidates(List.of(common, rare), EntityType.HUSK, NIGHT, this::weight));
        assertTrue(MobSelector.candidates(List.of(common, rare), EntityType.SKELETON, NIGHT, this::weight)
                .isEmpty());
        weights.put("rare", 0);
        assertEquals(
                List.of(common), MobSelector.candidates(List.of(common, rare), EntityType.ZOMBIE, NIGHT, this::weight));
        var day = new SpawnContext("world", ignored -> true, 64, 0, true, SpawnReason.NATURAL, false);
        assertTrue(MobSelector.candidates(List.of(common), EntityType.ZOMBIE, day, this::weight)
                .isEmpty());
    }

    @Test
    void theClosestTypeWins() {
        CustomMob zombie = Mobs.zombie("zombie", Graph.builder().build());
        CustomMob husk = new CustomMob(
                "husk",
                zombie.equipment(),
                new Attributes(
                        MobName.english("Husk"),
                        EntityType.HUSK,
                        false,
                        ValueModifier.DEFAULT,
                        2,
                        ValueModifier.DEFAULT,
                        2),
                zombie.behaviour(),
                zombie.loot(),
                zombie.extension(),
                zombie.spawn());
        List<CustomMob> both = List.of(zombie, husk);

        assertEquals(List.of(husk), MobSelector.candidates(both, EntityType.HUSK, NIGHT, ignored -> 1));
        assertEquals(List.of(zombie), MobSelector.candidates(both, EntityType.ZOMBIE, NIGHT, ignored -> 1));
        assertEquals(List.of(zombie), MobSelector.candidates(both, EntityType.DROWNED, NIGHT, ignored -> 1));
        assertEquals(1, MobSelector.distance(EntityType.CAVE_SPIDER, EntityType.SPIDER));
    }

    @Test
    void picksFollowTheWeights() {
        var random = new SplittableRandom(7);
        int rares = 0;
        for (int i = 0; i < 10_000; i++) {
            if (MobSelector.pick(List.of(common, rare), this::weight, random).orElseThrow() == rare) rares++;
        }
        assertTrue(rares > 850 && rares < 1150, "about a tenth: " + rares);
    }

    @Test
    void nothingToPickWithoutWeight() {
        weights.put("common", 0);
        weights.put("rare", 0);
        assertTrue(MobSelector.pick(List.of(common, rare), this::weight, new SplittableRandom())
                .isEmpty());
    }
}
