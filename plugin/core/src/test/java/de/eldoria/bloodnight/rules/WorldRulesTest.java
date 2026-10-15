/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldRulesTest extends RulesTest {
    @SuppressWarnings("removal")
    private static final GameRule<Boolean> SPAWN_PHANTOMS = GameRule.DO_INSOMNIA;

    private WorldRules rules;

    @BeforeEach
    void setUp() {
        settings = Settings.world("""
                {"nightRules":{
                  "difficulty":"HARD","monsterSpawnLimit":140,"weather":"THUNDER",
                  "gameRules":{"minecraft:spawn_phantoms":"false","minecraft:unknown":"1"},
                  "phantomsOnlyAtBloodNights":true}}
                """);
        rules = new WorldRules(
                nights,
                ignored -> settings.nightRules(),
                key -> key.equals("minecraft:spawn_phantoms") ? SPAWN_PHANTOMS : null,
                () -> true,
                Logger.getLogger("test"));
        server.getPluginManager().registerEvents(rules, plugin);
        world.setDifficulty(Difficulty.EASY);
        world.setSpawnLimit(SpawnCategory.MONSTER, 70);
        world.setStorm(false);
        world.setGameRule(SPAWN_PHANTOMS, true);
    }

    @Test
    void theWorldChangesForTheNightAndComesBack() {
        bloodNight();

        assertEquals(Difficulty.HARD, world.getDifficulty());
        assertEquals(140, world.getSpawnLimit(SpawnCategory.MONSTER));
        assertTrue(world.isThundering());
        assertFalse(world.getGameRuleValue(SPAWN_PHANTOMS));

        day();

        assertEquals(Difficulty.EASY, world.getDifficulty());
        assertEquals(70, world.getSpawnLimit(SpawnCategory.MONSTER));
        assertFalse(world.hasStorm());
        assertTrue(world.getGameRuleValue(SPAWN_PHANTOMS));
        assertFalse(world.getPersistentDataContainer().has(WorldRules.SNAPSHOT));
    }

    @Test
    void changesDuringTheNightStay() {
        bloodNight();
        world.setDifficulty(Difficulty.PEACEFUL);

        day();

        assertEquals(Difficulty.PEACEFUL, world.getDifficulty());
        assertEquals(70, world.getSpawnLimit(SpawnCategory.MONSTER));
    }

    @Test
    void leftoversComeBackAfterACrash() {
        bloodNight();
        HandlerList.unregisterAll(rules);
        WorldRules restarted = new WorldRules(
                nights, ignored -> settings.nightRules(), key -> null, () -> true, Logger.getLogger("test"));
        world.setFullTime(24_000);
        nights.check(List.of(world));

        restarted.restoreLeftovers(List.of(world));

        assertEquals(Difficulty.EASY, world.getDifficulty());
        assertEquals(70, world.getSpawnLimit(SpawnCategory.MONSTER));
    }

    @Test
    void phantomsOnlySpawnDuringBloodNights() {
        assertFalse(phantomSpawns());
        bloodNight();
        assertTrue(phantomSpawns());
    }

    private boolean phantomSpawns() {
        var phantom = world.spawnEntity(new Location(world, 0, 100, 0), EntityType.PHANTOM);
        var event = new CreatureSpawnEvent((LivingEntity) phantom, SpawnReason.NATURAL);
        rules.onSpawn(event);
        return !event.isCancelled();
    }
}
