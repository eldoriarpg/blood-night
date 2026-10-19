/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.spawn;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnRulesTest {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .build();

    private static SpawnContext spawn(String world, String biome, int y, int light, boolean sky, boolean night) {
        return new SpawnContext(world, biome::equals, y, light, sky, SpawnReason.NATURAL, night);
    }

    private static SpawnContext night() {
        return spawn("world", "minecraft:plains", 64, 0, true, true);
    }

    private static SpawnRules read(String json) {
        return MAPPER.readValue(json, SpawnRules.class);
    }

    @Test
    void missingFieldsGetTheDefaults() {
        SpawnRules rules = read("{}");
        assertEquals(SpawnRules.DEFAULT_WEIGHT, rules.weight());
        assertTrue(rules.nightOnly());
        assertEquals(NightEnd.REMOVE, rules.nightEnd());
        assertTrue(rules.allows(night()));
    }

    @Test
    void nightOnlyMobsNeedABloodNight() {
        assertFalse(SpawnRules.defaults().allows(spawn("world", "minecraft:plains", 64, 0, true, false)));
        assertTrue(read("{\"nightOnly\":false}").allows(spawn("world", "minecraft:plains", 64, 0, true, false)));
    }

    @Test
    void reasonsLimitTheSpawns() {
        SpawnRules rules = new SpawnRules(1, true, NightEnd.KEEP, Set.of(SpawnReason.SPAWNER), SpawnConditions.any());
        assertFalse(rules.allows(night()));
    }

    @Test
    void conditionsAreAllMet() {
        SpawnRules rules = read("""
                {"conditions":{"worlds":["World"],"biomes":["minecraft:plains"],"minY":0,"maxY":100,
                 "maxLight":7,"surface":"SKY_VISIBLE"}}
                """);
        assertTrue(rules.allows(night()));
        assertFalse(rules.allows(spawn("nether", "minecraft:plains", 64, 0, true, true)));
        assertFalse(rules.allows(spawn("world", "minecraft:desert", 64, 0, true, true)));
        assertFalse(rules.allows(spawn("world", "minecraft:plains", -5, 0, true, true)));
        assertFalse(rules.allows(spawn("world", "minecraft:plains", 120, 0, true, true)));
        assertFalse(rules.allows(spawn("world", "minecraft:plains", 64, 8, true, true)));
        assertFalse(rules.allows(spawn("world", "minecraft:plains", 64, 0, false, true)));
    }

    @Test
    void undergroundMeansNoSky() {
        SpawnRules rules = read("{\"conditions\":{\"surface\":\"UNDERGROUND\"}}");
        assertTrue(rules.allows(spawn("world", "minecraft:plains", 10, 0, false, true)));
        assertFalse(rules.allows(night()));
    }
}
