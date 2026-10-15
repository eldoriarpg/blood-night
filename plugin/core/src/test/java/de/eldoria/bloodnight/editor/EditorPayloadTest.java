/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.GraphJson;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.NodeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.IntNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorPayloadTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void carriesUserDataAndTheCatalogVersion() {
        Graph behaviour = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "print"))
                .node("print", NodeInstance.of("debug.print").literal("value", IntNode.valueOf(5)))
                .build();
        CustomMob mob = Mobs.zombie("test", behaviour);
        EditorPayload payload = new EditorPayload(
                "abc",
                Map.of("test", mob),
                List.of(),
                Map.of(),
                Map.of(),
                Behaviours.empty().problems(),
                Map.of(),
                Map.of("items.yml", "broken"),
                new SettingsPayload(new ConfigFile(), Map.of()),
                new EditorChoices(
                        "26.3",
                        List.of("minecraft:stone"),
                        Map.of("minecraft:diamond_sword", 1),
                        List.of("minecraft:sharpness"),
                        Map.of("CREEPER", "org.bukkit.entity.Creeper"),
                        List.of("CREEPER"),
                        List.of("SkeletalKnight"),
                        List.of("minecraft:max_health"),
                        List.of("minecraft:desert"),
                        List.of("minecraft:darkness"),
                        Map.of(
                                "minecraft:spawn_phantoms",
                                new EditorChoices.GameRuleChoice("boolean", "gamerule.doInsomnia")),
                        List.of("home", "spawn")),
                Map.of("mobs.yml", "abc"),
                EditorStore.empty(),
                false,
                null);

        JsonNode json = GraphJson.mapper().readTree(payload.asJson());

        assertEquals("abc", json.path("catalog").asString());
        assertEquals(
                "mob.tick",
                json.path("mobs")
                        .path("test")
                        .path("behaviour")
                        .path("nodes")
                        .path("tick")
                        .path("type")
                        .asString());
        assertEquals(
                5,
                json.path("mobs")
                        .path("test")
                        .path("behaviour")
                        .path("nodes")
                        .path("print")
                        .path("literals")
                        .path("value")
                        .asInt());
        assertEquals("broken", json.path("configProblems").path("items.yml").asString());
        assertFalse(json.path("setup").asBoolean());

        EditorLegacy legacy = new EditorLegacy(new ConfigFile(), Map.of(), Map.of(), Map.of(), List.of("no hats"));
        JsonNode setup = GraphJson.mapper().readTree(payload.forSetup(legacy).asJson());
        assertTrue(setup.path("setup").asBoolean());
        assertEquals("no hats", setup.path("legacy").path("notes").path(0).asString());
        assertFalse(
                setup.path("settings").path("main").path("setup").path("done").asBoolean(true));
    }

    @Test
    void theBundledCatalogIsAvailable() {
        assertTrue(BundledCatalog.json().orElseThrow().contains("\"event.damage_by_player\""));
    }

    @Test
    void theRuntimeCatalogKeepsTheBundledDescriptions() {
        Catalog catalog = RuntimeCatalog.create(
                BloodNightPlatform.create(CompileClasspath.discover(getClass().getClassLoader())));

        NodeType living = catalog.nodes().stream()
                .filter(node -> node.id().equals("living_entity.read"))
                .findFirst()
                .orElseThrow();
        assertTrue(living.outputs().stream()
                .anyMatch(port -> port.id().equals("absorption_amount")
                        && !port.description().isEmpty()));
        assertEquals(16, catalog.version().length());
    }
}
