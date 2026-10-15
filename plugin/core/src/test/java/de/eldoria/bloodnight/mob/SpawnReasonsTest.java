/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnReasonsTest {
    @Test
    void onlyFittingTypesSpawnByASpecialReason() {
        assertTrue(SpawnReasons.applies(SpawnReason.SLIME_SPLIT, EntityType.SLIME));
        assertTrue(SpawnReasons.applies(SpawnReason.SLIME_SPLIT, EntityType.MAGMA_CUBE));
        assertFalse(SpawnReasons.applies(SpawnReason.SLIME_SPLIT, EntityType.ZOMBIE));
        assertTrue(SpawnReasons.applies(SpawnReason.BREEDING, EntityType.COW));
        assertFalse(SpawnReasons.applies(SpawnReason.BREEDING, EntityType.CREEPER));
        assertTrue(SpawnReasons.applies(SpawnReason.REINFORCEMENTS, EntityType.HUSK));
        assertFalse(SpawnReasons.applies(SpawnReason.EXPLOSION, EntityType.CREEPER));
    }

    @Test
    void everyTypeSpawnsByTheCommonReasons() {
        List<String> creeper = SpawnReasons.byType(List.of("CREEPER")).get("CREEPER");

        assertTrue(creeper.containsAll(List.of("NATURAL", "SPAWNER", "COMMAND")));
        assertFalse(creeper.contains("BREEDING"));
    }
}
