/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.graph.BloodNightNodes;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMobsTest {
    private static GraphPlatform platform;
    private static List<CustomMob> mobs;

    @TempDir
    Path cacheDir;

    @BeforeAll
    static void build() {
        platform = BloodNightPlatform.create(CompileClasspath.discover(DefaultMobsTest.class.getClassLoader()));
        mobs = DefaultMobs.create(platform);
    }

    @Test
    void everyLegacyMobIsThere() {
        assertEquals(24, mobs.size());
        assertEquals(24, mobs.stream().map(CustomMob::id).distinct().count());
    }

    @Test
    void everyMobCompilesWithoutProblems() {
        Behaviours behaviours = new BehaviourService(
                        platform,
                        getClass().getClassLoader(),
                        new BytecodeCache(cacheDir),
                        "test",
                        Logger.getLogger("test"))
                .compileNow(mobs, List.of());

        assertEquals(Map.of(), behaviours.problems());
        mobs.forEach(mob -> assertTrue(behaviours.of(mob.id()).isPresent(), mob.id()));
    }

    @Test
    void particlesWithoutDataNeedNone() {
        MockBukkit.mock();
        try {
            List<String> needData = mobs.stream()
                    .flatMap(mob -> mob.behaviour().nodes().values().stream())
                    .filter(node -> node.type().equals(BloodNightNodes.WORLD_SPAWN_PARTICLE))
                    .map(node -> node.literals().get("particle").asString())
                    .filter(key -> Registry.PARTICLE_TYPE
                                    .getOrThrow(NamespacedKey.fromString(key))
                                    .getDataType()
                            != Void.class)
                    .toList();
            assertEquals(List.of(), needData);
        } finally {
            MockBukkit.unmock();
        }
    }
}
