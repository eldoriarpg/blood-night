/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutinesTest {
    private static NodeInstance.Builder define(String name) {
        return NodeInstance.of("routine.define").config("name", StringNode.valueOf(name));
    }

    private static NodeInstance.Builder parameter(String type, String routine, String name) {
        return NodeInstance.of("routine.parameter")
                .config("type", StringNode.valueOf(type))
                .config("routine", StringNode.valueOf(routine))
                .config("name", StringNode.valueOf(name));
    }

    /**
     * Routine {@code twice(value: number)} recording twice its value; two entries call it with 1 and 2.
     */
    private static Graph twice() {
        return Graph.builder()
                .node("first", NodeInstance.of("test.run").chain("next", "call_one"))
                .node("second", NodeInstance.of("test.run").chain("next", "call_two"))
                .node("call_one", NodeInstance.of("routine.twice").literal("value", DoubleNode.valueOf(1)))
                .node("call_two", NodeInstance.of("routine.twice").literal("value", DoubleNode.valueOf(2)))
                .node("twice", define("twice").chain("next", "record"))
                .node("value", parameter("number", "twice", "value"))
                .node(
                        "double",
                        NodeInstance.of("math.multiply")
                                .input("first", "value", "value")
                                .literal("second", DoubleNode.valueOf(2)))
                .node("record", NodeInstance.of("test.record").input("value", "double", "result"))
                .build();
    }

    @Test
    void entriesShareOneRoutine() throws GraphCompilationException {
        assertEquals(List.of(2.0, 4.0), Harness.run(twice(), 1));

        String source = Harness.compile(twice()).source();
        assertTrue(source.contains("private void twice(double value) {"), source);
        assertTrue(source.contains("twice(1.0d);"), source);
        assertTrue(source.contains("twice(2.0d);"), source);
        assertTrue(
                source.contains("this.context.record(value * 2.0d)") || source.contains("record((value * 2.0d))"),
                source);
    }

    @Test
    void routinesReturnEarly() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "call", "after"))
                .node("call", NodeInstance.of("routine.check").literal("count", IntNode.valueOf(3)))
                .node("after", NodeInstance.of("test.record").literal("value", StringNode.valueOf("after")))
                .node("check", define("check").chain("next", "branch"))
                .node("count", parameter("integer", "check", "count"))
                .node(
                        "big",
                        NodeInstance.of("logic.greater")
                                .input("first", "count", "value")
                                .literal("second", DoubleNode.valueOf(2)))
                .node(
                        "branch",
                        NodeInstance.of("flow.branch")
                                .input("condition", "big", "result")
                                .chain("true", "big_record"))
                .node("big_record", NodeInstance.of("test.record").literal("value", StringNode.valueOf("big")))
                .build();

        assertEquals(List.of("big", "after"), Harness.run(graph, 1));
        assertTrue(Harness.compile(graph).source().contains("if (!(count > 2.0d)) return;"));
    }

    @Test
    void routinesCannotCallThemselves() {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "call"))
                .node("call", NodeInstance.of("routine.loop"))
                .node("loop", define("loop").chain("next", "again"))
                .node("again", NodeInstance.of("routine.loop"))
                .build();

        GraphCompilationException error = assertThrows(GraphCompilationException.class, () -> Harness.compile(graph));
        assertTrue(error.getMessage().contains("Routine loop calls itself"), error.getMessage());
    }

    @Test
    void parametersAreOnlyVisibleInsideTheirRoutine() {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "call", "outside"))
                .node("call", NodeInstance.of("routine.show").literal("text", StringNode.valueOf("hi")))
                .node("show", define("show").chain("next", "inside"))
                .node("text", parameter("text", "show", "text"))
                .node("inside", NodeInstance.of("test.record").input("value", "text", "value"))
                .node("outside", NodeInstance.of("test.record").input("value", "text", "value"))
                .build();

        GraphCompilationException error = assertThrows(GraphCompilationException.class, () -> Harness.compile(graph));
        assertTrue(error.problems().stream().anyMatch(problem -> "outside".equals(problem.node())), error.getMessage());
    }

    @Test
    void routinesNeverCalledAreReported() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run"))
                .node("idle", define("idle").chain("next", "record"))
                .node("record", NodeInstance.of("test.record").literal("value", StringNode.valueOf("idle")))
                .build();

        List<GraphProblem> warnings = Harness.compile(graph).warnings();
        assertTrue(
                warnings.stream().anyMatch(problem -> problem.message().contains("never called")), warnings.toString());
    }

    @Test
    void routinesCannotHideMethodsOfTheHost() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("test.run").chain("next", "call"))
                .node("call", NodeInstance.of("routine.run"))
                .node("run", define("run"))
                .build();

        GraphCompilationException error = assertThrows(GraphCompilationException.class, () -> Harness.compile(graph));
        assertTrue(error.getMessage().contains("taken by a method"), error.getMessage());
    }
}
