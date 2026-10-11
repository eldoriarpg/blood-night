package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.ClassName;
import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphCompiler;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyGraphsTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static JsonNode legacy(String json) {
        return JSON.readTree(json);
    }

    @Test
    void oldEditorGraphMigratesCompilesAndRuns() throws IOException, GraphCompilationException {
        JsonNode editor;
        try (InputStream in = getClass().getResourceAsStream("/legacy-editor.json")) {
            editor = JSON.readTree(in);
        }

        LegacyGraphs.Migration migration = LegacyGraphs.migrate(editor.path("mobs").path("mobs").path("test").path("nodes"));
        Graph graph = migration.graph();

        assertTrue(migration.warnings().isEmpty(), migration.warnings().toString());
        assertEquals("value.integer", graph.nodes().get("0").type());
        assertEquals(5, graph.nodes().get("0").config().get("value").asInt());
        assertEquals("mob.tick", graph.nodes().get("4").type());
        assertEquals(List.of("3"), graph.nodes().get("4").executions().get("next"));
        assertEquals(0, graph.nodes().get("4").editor().path("position").path("x").asInt());

        CompiledBehaviour behaviour = new GraphCompiler<>(
                BloodNightPlatform.create(CompileClasspath.discover(getClass().getClassLoader())),
                CompiledBehaviour.class, getClass().getClassLoader())
                .compile(graph, ClassName.get("generated.legacy", "Test"))
                .instantiate(new TestContext(null));
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            behaviour.onTick();
        } finally {
            System.setOut(original);
        }
        assertEquals("15.0", buffer.toString(StandardCharsets.UTF_8).strip());
    }

    @Test
    void portsAndControlFlowAreRenamed() {
        Graph graph = LegacyGraphs.migrate(legacy("""
                {"nodes": {
                  "0": {"nodeClass": "de.eldoria.bloodnight.nodes.trigger.impl.events.damage.OnDamageByPlayerNode",
                        "input": {}, "executions": {"nextNodes": {"next": [1]}}, "id": 0},
                  "1": {"nodeClass": "de.eldoria.bloodnight.nodes.controlflow.impl.BatchNode",
                        "input": {"value": {"node": 2, "name": "value"}}, "executions": {"nextNodes": {"next": [3]}}, "id": 1},
                  "2": {"nodeClass": "de.eldoria.bloodnight.nodes.value.impl.IntegerNode", "input": {}, "value": 3, "id": 2},
                  "3": {"nodeClass": "de.eldoria.bloodnight.nodes.action.impl.PrintNode",
                        "input": {"value": {"node": 0, "name": "damage_cause"}}, "id": 3},
                  "4": {"nodeClass": "de.eldoria.bloodnight.nodes.value.impl.EntityTypeNode", "input": {}, "value": "ZOMBIE", "id": 4},
                  "5": {"nodeClass": "de.eldoria.bloodnight.nodes.transform.impl.deconstruction.SplitEntityNode",
                        "input": {"value": {"node": 0, "name": "target"}}, "id": 5},
                  "6": {"nodeClass": "de.eldoria.bloodnight.nodes.action.impl.PrintNode",
                        "input": {"value": {"node": 5, "name": "entity_type"}}, "id": 6}
                }}
                """)).graph();

        NodeInstance batch = graph.nodes().get("1");
        assertEquals("flow.batch", batch.type());
        assertEquals("count", batch.inputs().keySet().iterator().next());
        assertEquals(List.of("3"), batch.executions().get("body"));
        assertEquals("cause", graph.nodes().get("3").inputs().get("value").port());
        assertEquals("minecraft:zombie", graph.nodes().get("4").config().get("value").asString());
        assertEquals("target", graph.nodes().get("5").inputs().keySet().iterator().next());
        assertEquals("entity", graph.nodes().get("5").inputs().get("target").port());
        assertEquals("type", graph.nodes().get("6").inputs().get("value").port());
    }

    @Test
    void droppedAndUnknownPartsAreReported() {
        LegacyGraphs.Migration migration = LegacyGraphs.migrate(legacy("""
                {"nodes": {
                  "0": {"nodeClass": "de.eldoria.bloodnight.nodes.trigger.impl.events.damage.OnDeathNode", "input": {}, "id": 0},
                  "1": {"nodeClass": "de.eldoria.bloodnight.nodes.action.impl.PrintNode",
                        "input": {"value": {"node": 0, "name": "killer"}}, "id": 1},
                  "2": {"nodeClass": "com.example.CustomNode", "input": {}, "id": 2}
                }}
                """));

        assertEquals(2, migration.warnings().size(), migration.warnings().toString());
        assertTrue(migration.graph().nodes().get("1").inputs().isEmpty());
        assertEquals("com.example.CustomNode", migration.graph().nodes().get("2").type());
    }
}
