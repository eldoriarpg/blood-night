/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.types.TypeMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static de.eldoria.graph.std.Harness.evaluate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardNodesTest {

    @Test
    void values() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "a", "b", "c", "d", "e"))
                .node("a", NodeInstance.of("test.record").input("value", "bool", "value"))
                .node("b", NodeInstance.of("test.record").input("value", "int", "value"))
                .node("c", NodeInstance.of("test.record").input("value", "number", "value"))
                .node("d", NodeInstance.of("test.record").input("value", "text", "value"))
                .node("e", NodeInstance.of("test.record").input("value", "default", "value"))
                .node("bool", Harness.literal(true))
                .node("int", Harness.literal(-3))
                .node("number", Harness.literal(2.5))
                .node("text", Harness.literal("Chojo stinkt"))
                .node("default", NodeInstance.of("value.number"))
                .build();

        assertEquals(List.of(true, -3, 2.5, "Chojo stinkt", 0.0), Harness.run(graph, 1));
    }

    @ParameterizedTest
    @CsvSource({
        "math.add, 5.0, 2.0, 7.0",
        "math.subtract, 5.0, 2.0, 3.0",
        "math.multiply, 5.0, 2.0, 10.0",
        "math.divide, 5.0, 2.0, 2.5",
        "math.divide, 1.0, 0.0, Infinity"
    })
    void arithmetic(String type, double first, double second, double result) throws GraphCompilationException {
        assertEquals(result, evaluate(type, Map.of("first", first, "second", second)));
    }

    @Test
    void integersWidenToNumbers() throws GraphCompilationException {
        assertEquals(7.0, evaluate("math.add", Map.of("first", 5, "second", 2)));
    }

    @ParameterizedTest
    @CsvSource({
        "math.abs, -2.5, 2.5",
        "math.abs_integer, -2.7, 2",
        "math.ceil, 2.1, 3",
        "math.floor, 2.9, 2",
        "math.floor, -2.1, -3",
        "math.round, 2.5, 3",
        "math.round, 2.4, 2"
    })
    void rounding(String type, double value, String result) throws GraphCompilationException {
        Object actual = evaluate(type, Map.of("value", value));
        assertEquals(result, String.valueOf(actual));
    }

    @ParameterizedTest
    @CsvSource({
        "math.clamp, 5.0, 0.0, 10.0, 5.0",
        "math.clamp, -5.0, 0.0, 10.0, 0.0",
        "math.clamp, 15.0, 0.0, 10.0, 10.0",
        "math.clamp_integer, 7.9, 0.0, 10.0, 7",
        "math.clamp_integer, 15.0, 0.0, 10.0, 10"
    })
    void clamp(String type, double value, double lower, double upper, String result) throws GraphCompilationException {
        assertEquals(result, String.valueOf(evaluate(type, Map.of("value", value, "lower", lower, "upper", upper))));
    }

    @ParameterizedTest
    @CsvSource({
        "logic.and, true, true, true",
        "logic.and, true, false, false",
        "logic.or, false, true, true",
        "logic.or, false, false, false"
    })
    void booleans(String type, boolean first, boolean second, boolean result) throws GraphCompilationException {
        assertEquals(result, evaluate(type, Map.of("first", first, "second", second)));
    }

    @Test
    void not() throws GraphCompilationException {
        assertEquals(false, evaluate("logic.not", Map.of("value", true)));
    }

    @ParameterizedTest
    @CsvSource({
        "logic.greater, 2.0, 1.0, true",
        "logic.greater, 1.0, 1.0, false",
        "logic.greater_or_equal, 1.0, 1.0, true",
        "logic.less, 1.0, 2.0, true",
        "logic.less, 1.0, 1.0, false",
        "logic.less_or_equal, 1.0, 1.0, true"
    })
    void comparisons(String type, double first, double second, boolean result) throws GraphCompilationException {
        assertEquals(result, evaluate(type, Map.of("first", first, "second", second)));
    }

    @Test
    void equality() throws GraphCompilationException {
        assertEquals(true, evaluate("logic.equal", Map.of("first", "a", "second", "a")));
        assertEquals(false, evaluate("logic.equal", Map.of("first", "a", "second", "b")));
        assertEquals(false, evaluate("logic.equal", Map.of("first", 1, "second", 1.0)));
        assertEquals(false, evaluate("logic.is_null", Map.of("value", "a")));
    }

    @Test
    void randomStaysInBounds() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "int", "number"))
                .node("int", NodeInstance.of("test.record").input("value", "random_int", "result"))
                .node("number", NodeInstance.of("test.record").input("value", "random_number", "result"))
                .node(
                        "random_int",
                        NodeInstance.of("function.random_integer")
                                .input("lower", "low_int", "value")
                                .input("upper", "high_int", "value"))
                .node(
                        "random_number",
                        NodeInstance.of("function.random_number")
                                .input("lower", "low", "value")
                                .input("upper", "high", "value"))
                .node("low_int", Harness.literal(1))
                .node("high_int", Harness.literal(3))
                .node("low", Harness.literal(1.0))
                .node("high", Harness.literal(2.0))
                .build();

        List<Object> values = Harness.run(graph, 200);
        List<Integer> integers = values.stream()
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast)
                .toList();
        List<Double> numbers = values.stream()
                .filter(Double.class::isInstance)
                .map(Double.class::cast)
                .toList();

        assertEquals(List.of(1, 2, 3), integers.stream().distinct().sorted().toList());
        assertTrue(numbers.stream().allMatch(number -> number >= 1.0 && number < 2.0));
    }

    @Test
    void cooldownLetsTheFirstEvaluationPass() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "cooldown", "result"))
                .node("cooldown", NodeInstance.of("function.cooldown").input("value", "millis", "value"))
                .node("millis", Harness.literal(60_000))
                .build();

        assertEquals(List.of(true, false, false), Harness.run(graph, 3));
    }

    @Test
    void cooldownStateIsPerInstance() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "cooldown", "result"))
                .node("cooldown", NodeInstance.of("function.cooldown").input("value", "millis", "value"))
                .node("millis", Harness.literal(60_000))
                .build();
        var compiled = Harness.compile(graph);
        Recorder first = new Recorder();
        Recorder second = new Recorder();

        compiled.instantiate(first).run();
        compiled.instantiate(second).run();

        assertEquals(List.of(true), first.values());
        assertEquals(List.of(true), second.values());
    }

    @Test
    void branch() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "branch"))
                .node(
                        "branch",
                        NodeInstance.of("flow.branch")
                                .input("condition", "yes", "value")
                                .chain("true", "on_true")
                                .chain("false", "on_false")
                                .chain("next", "after"))
                .node("yes", Harness.literal(true))
                .node("on_true", NodeInstance.of("test.record").input("value", "t", "value"))
                .node("on_false", NodeInstance.of("test.record").input("value", "f", "value"))
                .node("after", NodeInstance.of("test.record").input("value", "a", "value"))
                .node("t", Harness.literal("true"))
                .node("f", Harness.literal("false"))
                .node("a", Harness.literal("after"))
                .build();

        assertEquals(List.of("true", "after"), Harness.run(graph, 1));
    }

    @Test
    void batchRepeatsBodyWithIndex() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "batch"))
                .node(
                        "batch",
                        NodeInstance.of("flow.batch")
                                .input("count", "three", "value")
                                .chain("body", "index")
                                .chain("next", "done"))
                .node("three", Harness.literal(3))
                .node("index", NodeInstance.of("test.record").input("value", "batch", "index"))
                .node("done", NodeInstance.of("test.record").input("value", "end", "value"))
                .node("end", Harness.literal("done"))
                .build();

        assertEquals(List.of(0, 1, 2, "done"), Harness.run(graph, 1));
    }

    @Test
    void forEachInfersTheElementType() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "loop"))
                .node("text", Harness.literal("a,b,c"))
                .node("split", NodeInstance.of("test.split").input("value", "text", "value"))
                .node(
                        "loop",
                        NodeInstance.of("flow.for_each")
                                .input("list", "split", "result")
                                .chain("body", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "upper", "result"))
                .node("upper", NodeInstance.of("test.upper").input("value", "loop", "element"))
                .build();

        assertEquals(List.of("A", "B", "C"), Harness.runWith(graph, ListNodes.ALL));
    }

    @Test
    void forEachElementIsNotVisibleAfterTheLoop() {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "loop"))
                .node("text", Harness.literal("a"))
                .node("split", NodeInstance.of("test.split").input("value", "text", "value"))
                .node(
                        "loop",
                        NodeInstance.of("flow.for_each")
                                .input("list", "split", "result")
                                .chain("next", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "loop", "element"))
                .build();

        GraphCompilationException exception =
                assertThrows(GraphCompilationException.class, () -> Harness.runWith(graph, ListNodes.ALL));

        assertTrue(
                exception.problems().stream()
                        .anyMatch(problem ->
                                problem.severity() == GraphProblem.Severity.ERROR && "record".equals(problem.node())),
                exception.getMessage());
    }

    @Test
    void castsNarrowValues() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node(
                        "run",
                        NodeInstance.of("test.run")
                                .chain("next", "text_matches", "text_result", "number_matches", "number_result"))
                .node("text", Harness.literal("hello"))
                .node("number", Harness.literal(4))
                .node("as_text", NodeInstance.of("cast.text").input("value", "text", "value"))
                .node("number_as_text", NodeInstance.of("cast.text").input("value", "number", "value"))
                .node("text_matches", NodeInstance.of("test.record").input("value", "as_text", "matches"))
                .node("text_result", NodeInstance.of("test.record").input("value", "as_text", "result"))
                .node("number_matches", NodeInstance.of("test.record").input("value", "number_as_text", "matches"))
                .node("number_result", NodeInstance.of("test.record").input("value", "number_as_text", "result"))
                .build();

        assertEquals(Arrays.asList(true, "hello", false, null), Harness.run(graph, 1));
    }

    @Test
    void castsSkipPrimitiveTypes() {
        assertEquals(
                List.of("cast.text"),
                StandardNodes.casts(TypeMapping.defaults()).stream()
                        .map(NodeDefinition::id)
                        .toList());
    }

    @Test
    void switchRunsTheMatchingCase() throws GraphCompilationException {
        for (int value : new int[] {0, 5, 9}) {
            Graph graph = Graph.builder()
                    .node("run", NodeInstance.of("test.run").chain("next", "switch"))
                    .node(
                            "switch",
                            NodeInstance.of("flow.switch")
                                    .literal("value", IntNode.valueOf(value))
                                    .chain("case_0", "zero")
                                    .chain("case_5", "five")
                                    .chain("default", "other")
                                    .chain("next", "after"))
                    .node("zero", NodeInstance.of("test.record").literal("value", StringNode.valueOf("zero")))
                    .node("five", NodeInstance.of("test.record").literal("value", StringNode.valueOf("five")))
                    .node("other", NodeInstance.of("test.record").literal("value", StringNode.valueOf("other")))
                    .node("after", NodeInstance.of("test.record").literal("value", StringNode.valueOf("after")))
                    .build();

            String expected = value == 0 ? "zero" : value == 5 ? "five" : "other";
            assertEquals(List.of(expected, "after"), Harness.run(graph, 1));
        }
    }

    @Test
    void chanceFollowsItsPercentage() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "never", "always"))
                .node("zero", NodeInstance.of("function.chance").literal("percent", DoubleNode.valueOf(0)))
                .node("hundred", NodeInstance.of("function.chance").literal("percent", DoubleNode.valueOf(100)))
                .node("never", NodeInstance.of("test.record").input("value", "zero", "result"))
                .node("always", NodeInstance.of("test.record").input("value", "hundred", "result"))
                .build();

        assertEquals(List.of(false, true, false, true), Harness.run(graph, 2));
    }

    @Test
    void printWritesToStandardOut() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "print"))
                .node("print", NodeInstance.of("debug.print").input("value", "text", "value"))
                .node("text", Harness.literal("hello"))
                .build();
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            Harness.run(graph, 1);
        } finally {
            System.setOut(original);
        }

        assertEquals("hello", buffer.toString(StandardCharsets.UTF_8).strip());
    }

    @Test
    void timersWithTheSameNameShareTheirState() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "reset", "fresh"))
                .node("reset", NodeInstance.of("timer.reset").config("timer", StringNode.valueOf("hit")))
                .node("elapsed", NodeInstance.of("timer.elapsed").config("timer", StringNode.valueOf("hit")))
                .node("other", NodeInstance.of("timer.elapsed").config("timer", StringNode.valueOf("shot")))
                .node(
                        "less",
                        NodeInstance.of("logic.less_or_equal")
                                .input("first", "elapsed", "seconds")
                                .input("second", "other", "seconds"))
                .node("fresh", NodeInstance.of("test.record").input("value", "less", "result"))
                .build();

        assertEquals(List.of(true), Harness.run(graph, 1));
        String source = Harness.compile(graph).source();
        assertEquals(2, source.split("new Timer\\(\\)", -1).length - 1, source);
    }
}
