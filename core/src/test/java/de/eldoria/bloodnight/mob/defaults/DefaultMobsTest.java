/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.platform.GraphPlatform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void everyLegacyMobNewMobsAndTheBloodDragonAreThere() {
        assertEquals(30, mobs.size());
        assertEquals(30, mobs.stream().map(CustomMob::id).distinct().count());
        assertTrue(mobs.stream().anyMatch(mob -> mob.id().equals(DefaultMobs.BLOOD_DRAGON)));
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
        String speedZombie = behaviours.of("speed_zombie").orElseThrow().source();
        assertTrue(
                speedZombie.contains(
                        "private static final PotionEffectType SPEED = Registries.entry(RegistryKey.MOB_EFFECT, \"minecraft:speed\");"),
                speedZombie);
        mobs.forEach(mob -> {
            String source = behaviours.of(mob.id()).orElseThrow().source();
            assertFalse(source.contains("__v"), source);
            assertFalse(source.matches("(?s).*else \\{\\s*}.*"), source);
        });
    }

    @Test
    void everyParticleGetsTheDataItNeeds() {
        MockBukkit.mock();
        try {
            assertEquals(30, DefaultMobs.create(platform).size());
        } finally {
            MockBukkit.unmock();
        }
    }
}
