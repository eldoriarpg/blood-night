/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphJsonTest {

    private static Graph sample() {
        ObjectNode position = JsonNodeFactory.instance.objectNode().put("x", 10).put("y", -4);
        return Graph.builder()
                .node("tick", NodeInstance.of("entry.tick").chain("next", "print", "other"))
                .node("five", NodeInstance.of("value.number").config("value", DoubleNode.valueOf(5)))
                .node(
                        "text",
                        NodeInstance.of("value.text").config("value", StringNode.valueOf("\"); System.exit(0); //")))
                .node(
                        "add",
                        NodeInstance.of("math.add").input("a", "five", "value").literal("b", DoubleNode.valueOf(2)))
                .node(
                        "print",
                        NodeInstance.of("debug.print")
                                .input("value", "add", "result")
                                .editor(position))
                .node("other", NodeInstance.of("debug.print"))
                .build();
    }

    @Test
    void roundTrip() {
        Graph graph = sample();

        Graph read = GraphJson.read(GraphJson.write(graph));

        assertEquals(graph, read);
    }

    @Test
    void outputIsOrderedAndOmitsEmptyParts() {
        String json = GraphJson.write(sample());

        assertTrue(json.startsWith("{\"nodes\":{\"add\":"), json);
        assertTrue(json.contains("\"other\":{\"type\":\"debug.print\"}"), json);
        assertTrue(json.contains("\"executions\":{\"next\":[\"print\",\"other\"]}"), json);
    }

    @Test
    void keepsEditorStateUntouched() {
        Graph read = GraphJson.read(GraphJson.write(sample()));

        assertEquals(-4, read.nodes().get("print").editor().get("y").asInt());
    }

    @Test
    void executionOrderIsKept() {
        Graph read = GraphJson.read(GraphJson.write(sample()));

        assertEquals(
                List.of("print", "other"), read.nodes().get("tick").executions().get("next"));
    }

    @Test
    void macrosRoundTrip() {
        Macro macro = new Macro("double_it", "Double It", "Adds a number to itself", null, sample());

        Macro read = GraphJson.readMacro(GraphJson.writeMacro(macro));

        assertEquals(macro, read);
        assertEquals("macro", read.category());
    }

    @Test
    void rejectsUnknownProperties() {
        assertThrows(
                JacksonException.class,
                () -> GraphJson.read("{\"nodes\":{\"a\":{\"type\":\"x\",\"nodeClass\":\"y\"}}}"));
    }

    @Test
    void rejectsDuplicateIdsInBuilder() {
        Graph.Builder builder = Graph.builder().node("a", NodeInstance.of("x"));

        assertThrows(IllegalArgumentException.class, () -> builder.node("a", NodeInstance.of("y")));
    }

    @Test
    void rejectsBlankIds() {
        assertThrows(JacksonException.class, () -> GraphJson.read("{\"nodes\":{\" \":{\"type\":\"x\"}}}"));
    }
}
