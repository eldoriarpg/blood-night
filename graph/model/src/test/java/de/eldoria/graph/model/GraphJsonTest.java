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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertFalse(json.contains("comments"), json);
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
    void commentsRoundTripAndKeepTheirFrame() {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("entry.tick"))
                .comment(
                        "charge", new Comment("Charge attack", "Every 5 seconds", "red", 10, -20, 300, 200, true, null))
                .build();

        Graph read = GraphJson.read(GraphJson.write(graph));

        assertEquals(graph, read);
        assertEquals(
                new Comment("Charge attack", "Every 5 seconds", "red", 10, -20, 300, 200, true, null),
                read.comments().get("charge"));
    }

    @Test
    void commentsMissingPartsReadAsEmpty() {
        Graph read = GraphJson.read("{\"nodes\":{},\"comments\":{\"c\":{\"x\":1,\"y\":2}}}");

        assertEquals(
                new Comment("", "", "grey", 1, 2, 0, 0, false, null),
                read.comments().get("c"));
    }

    @Test
    void macrosRoundTrip() {
        Macro macro = new Macro("double_it", "Double It", "Adds a number to itself", "macro", sample());

        Macro read = GraphJson.readMacro(GraphJson.writeMacro(macro));

        assertEquals(macro, read);
        assertEquals("macro", read.category());
    }

    @Test
    void macrosFillInMissingFields() {
        Macro read = GraphJson.readMacro("{\"id\":\"bare\",\"body\":{\"nodes\":{}}}");

        assertEquals(Macro.of("bare", Graph.builder().build()), read);
        assertEquals("bare", read.name());
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

    @Test
    void statesAndTheNodesInThemSurviveTheRoundTrip() {
        Graph graph = Graph.builder()
                .state(new State("fight", null, true))
                .state(new State("calm", "fight", true))
                .node("start", NodeInstance.of("mob.tick").state("calm"))
                .comment("c", new Comment("", "", "grey", 0, 0, 0, 0, false, "calm"))
                .build();

        Graph read = GraphJson.read(GraphJson.write(graph));

        assertEquals(graph.states(), read.states());
        assertEquals("calm", read.nodes().get("start").state());
        assertEquals("calm", read.comments().get("c").state());
    }
}
