package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.GraphJson;
import de.eldoria.graph.model.NodeInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.IntNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        EditorPayload payload = new EditorPayload("abc", Map.of("test", mob), List.of(), Map.of(), Behaviours.empty().problems(),
                Map.of(), Map.of("items.yml", "broken"));

        JsonNode json = GraphJson.mapper().readTree(payload.asJson());

        assertEquals("abc", json.path("catalog").asString());
        assertEquals("mob.tick", json.path("mobs").path("test").path("behaviour").path("nodes").path("tick").path("type").asString());
        assertEquals(5, json.path("mobs").path("test").path("behaviour").path("nodes").path("print").path("literals").path("value").asInt());
        assertEquals("broken", json.path("configProblems").path("items.yml").asString());
        assertTrue(json.path("mobs").path("test").path("migrationNotes").isMissingNode());
    }

    @Test
    void theBundledCatalogIsAvailable() {
        String json = BundledCatalog.json().orElseThrow();

        assertTrue(json.contains("\"event.damage_by_player\""));
        assertEquals(16, BundledCatalog.version().length());
    }
}
