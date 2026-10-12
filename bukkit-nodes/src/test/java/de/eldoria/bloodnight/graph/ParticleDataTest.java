/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.graph.compiler.Graphs;
import de.eldoria.graph.platform.GraphPlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleDataTest {
    private static GraphPlatform platform;

    @BeforeAll
    static void platform() {
        platform = BloodNightPlatform.create(CompileClasspath.discover(ParticleDataTest.class.getClassLoader()));
    }

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static Graphs spawn(String type, String particle, Consumer<Graphs.NodeBuilder> data) {
        Graphs graphs = Graphs.with(platform);
        graphs.node("tick", BloodNightNodes.MOB_TICK).chain("next", "spawn");
        graphs.node("self", BloodNightNodes.MOB_SELF);
        graphs.node("get", BloodNightNodes.ENTITY_READ).input("target", "self", "entity");
        Graphs.NodeBuilder spawn = graphs.node("spawn", type)
                .input("target", "get", "world")
                .literal("particle", particle)
                .input("location", "get", "location")
                .literal("count", 1)
                .literal("offset_x", 0.0)
                .literal("offset_y", 0.0)
                .literal("offset_z", 0.0);
        data.accept(spawn);
        return graphs;
    }

    private static Graphs withPower(String particle) {
        Graphs graphs =
                spawn(BloodNightNodes.WORLD_SPAWN_PARTICLE, particle, spawn -> spawn.input("data", "power", "power"));
        graphs.node("power", BloodNightNodes.PARTICLE_POWER).literal("power", 1.0);
        return graphs;
    }

    private static String problem(Graphs graphs) {
        return assertThrows(IllegalArgumentException.class, graphs::build).getMessage();
    }

    @Test
    void particlesWithoutDataSpawnPlainly() {
        assertDoesNotThrow(spawn(BloodNightNodes.WORLD_SPAWN_PARTICLE, "minecraft:flame", spawn -> {})::build);
    }

    @Test
    void particlesNeedingDataAreRefusedWithoutIt() {
        String problem = problem(spawn(BloodNightNodes.WORLD_SPAWN_PARTICLE, "minecraft:dragon_breath", spawn -> {}));
        assertTrue(problem.contains("Float") && problem.contains("Particle Power"), problem);
    }

    @Test
    void dataOfTheRightTypeIsAccepted() {
        assertDoesNotThrow(withPower("minecraft:dragon_breath")::build);
    }

    @Test
    void dataOfAnotherTypeIsRefused() {
        String problem = problem(withPower("minecraft:instant_effect"));
        assertTrue(problem.contains("Spell") && problem.contains("Float"), problem);
    }

    @Test
    void dataForParticlesTakingNoneIsRefused() {
        String problem = problem(
                spawn(BloodNightNodes.WORLD_SPAWN_PARTICLE, "minecraft:flame", spawn -> spawn.literal("data", 1)));
        assertTrue(problem.contains("takes no data"), problem);
    }

    @Test
    void dataCannotBeTypedIn() {
        String problem = problem(spawn(
                BloodNightNodes.WORLD_SPAWN_PARTICLE, "minecraft:dragon_breath", spawn -> spawn.literal("data", 1.0)));
        assertTrue(problem.contains("cannot be typed in"), problem);
    }
}
