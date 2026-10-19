/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.types.TypeMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.node.BooleanNode;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardNodesTest {

    @Test
    void values() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "a", "b", "c", "d"))
                .node("a", NodeInstance.of("test.record").input("value", "bool", "value"))
                .node("b", NodeInstance.of("test.record").input("value", "int", "value"))
                .node("c", NodeInstance.of("test.record").input("value", "number", "value"))
                .node("d", NodeInstance.of("test.record").input("value", "text", "value"))
                .node("bool", Harness.literal(true))
                .node("int", Harness.literal(-3))
                .node("number", Harness.literal(2.5))
                .node("text", Harness.literal("Chojo stinkt"))
                .build();

        assertEquals(List.of(true, -3, 2.5, "Chojo stinkt"), Harness.run(graph, 1));
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
    void mathKeepsWholeNumbersWholeAndWidensToDecimals() throws GraphCompilationException {
        assertEquals(7, evaluate("math.add", Map.of("first", 5, "second", 2)));
        assertEquals(7.5, evaluate("math.add", Map.of("first", 5, "second", 2.5)));
        assertEquals(2, evaluate("math.abs", Map.of("value", -2)));
        assertEquals(7, evaluate("math.clamp", Map.of("value", 7, "lower", 0, "upper", 10)));
        assertEquals(10.0, evaluate("math.clamp", Map.of("value", 15.5, "lower", 0, "upper", 10)));
    }

    @ParameterizedTest
    @CsvSource({
        "math.abs, -2.5, 2.5",
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
        "math.clamp, 15.0, 0.0, 10.0, 10.0"
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
                .node("cooldown", NodeInstance.of("function.cooldown").input("duration", "length", "value"))
                .node("length", Harness.literal(60))
                .build();

        assertEquals(List.of(true, false, false), Harness.run(graph, 3));
    }

    @Test
    void cooldownStateIsPerInstance() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "cooldown", "result"))
                .node("cooldown", NodeInstance.of("function.cooldown").input("duration", "length", "value"))
                .node("length", Harness.literal(60))
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
                .node(
                        "as_text",
                        NodeInstance.of("cast")
                                .config("type", StringNode.valueOf("text"))
                                .input("value", "text", "value"))
                .node(
                        "number_as_text",
                        NodeInstance.of("cast")
                                .config("type", StringNode.valueOf("text"))
                                .input("value", "number", "value"))
                .node("text_matches", NodeInstance.of("test.record").input("value", "as_text", "matches"))
                .node("text_result", NodeInstance.of("test.record").input("value", "as_text", "result"))
                .node("number_matches", NodeInstance.of("test.record").input("value", "number_as_text", "matches"))
                .node("number_result", NodeInstance.of("test.record").input("value", "number_as_text", "result"))
                .build();

        assertEquals(Arrays.asList(true, "hello", false, null), Harness.run(graph, 1));
    }

    @Test
    void castsOfferReferenceTypesAndValuesTypesWithALiteralForm() {
        List<NodeDefinition> typed = StandardNodes.typed(TypeMapping.defaults());
        assertEquals(
                List.of("text"),
                typed.getFirst().descriptor().configField("type").orElseThrow().choices());
        assertEquals(
                List.of("boolean", "integer", "number", "text"),
                typed.get(1).descriptor().configField("type").orElseThrow().choices());
    }

    @Test
    void switchRunsTheMatchingCase() throws GraphCompilationException {
        for (int value : new int[] {3, 5, 9}) {
            Graph graph = Graph.builder()
                    .node("run", NodeInstance.of("test.run").chain("next", "switch"))
                    .node(
                            "switch",
                            NodeInstance.of("flow.switch")
                                    .literal("value", IntNode.valueOf(value))
                                    .config("case_0", IntNode.valueOf(3))
                                    .config("case_1", IntNode.valueOf(5))
                                    .config("case_2", IntNode.valueOf(9))
                                    .chain("case_0", "three")
                                    .chain("case_1", "five")
                                    .chain("default", "other")
                                    .chain("next", "after"))
                    .node("three", NodeInstance.of("test.record").literal("value", StringNode.valueOf("three")))
                    .node("five", NodeInstance.of("test.record").literal("value", StringNode.valueOf("five")))
                    .node("other", NodeInstance.of("test.record").literal("value", StringNode.valueOf("other")))
                    .node("after", NodeInstance.of("test.record").literal("value", StringNode.valueOf("after")))
                    .build();

            String expected = value == 3 ? "three" : value == 5 ? "five" : "other";
            assertEquals(List.of(expected, "after"), Harness.run(graph, 1));
        }
    }

    @Test
    void switchMatchesTexts() throws GraphCompilationException {
        for (String value : new String[] {"fire", "ice", "wind"}) {
            Graph graph = Graph.builder()
                    .node("run", NodeInstance.of("test.run").chain("next", "switch"))
                    .node(
                            "switch",
                            NodeInstance.of("flow.switch")
                                    .config("type", StringNode.valueOf("text"))
                                    .literal("value", StringNode.valueOf(value))
                                    .config("case_0", StringNode.valueOf("fire"))
                                    .config("case_3", StringNode.valueOf("ice"))
                                    .chain("case_0", "burn")
                                    .chain("case_3", "freeze")
                                    .chain("default", "other"))
                    .node("burn", NodeInstance.of("test.record").literal("value", StringNode.valueOf("burn")))
                    .node("freeze", NodeInstance.of("test.record").literal("value", StringNode.valueOf("freeze")))
                    .node("other", NodeInstance.of("test.record").literal("value", StringNode.valueOf("other")))
                    .build();

            String expected = value.equals("fire") ? "burn" : value.equals("ice") ? "freeze" : "other";
            assertEquals(List.of(expected), Harness.run(graph, 1));
        }
    }

    @Test
    void switchSkipsCasesWithoutAValue() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "switch"))
                .node(
                        "switch",
                        NodeInstance.of("flow.switch")
                                .literal("value", IntNode.valueOf(0))
                                .chain("case_0", "never")
                                .chain("default", "other"))
                .node("never", NodeInstance.of("test.record").literal("value", StringNode.valueOf("never")))
                .node("other", NodeInstance.of("test.record").literal("value", StringNode.valueOf("other")))
                .build();

        assertEquals(List.of("other"), Harness.run(graph, 1));
    }

    private static Graph delayed(String type) {
        return Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "wait", "now"))
                .node(
                        "wait",
                        NodeInstance.of(type)
                                .config("unit", StringNode.valueOf("MILLISECONDS"))
                                .literal("duration", IntNode.valueOf(0))
                                .chain("next", "later"))
                .node("later", NodeInstance.of("test.record").literal("value", StringNode.valueOf("later")))
                .node("now", NodeInstance.of("test.record").literal("value", StringNode.valueOf("now")))
                .build();
    }

    @Test
    void delayRunsTheRestOfItsChainOnTheClock() throws GraphCompilationException {
        Recorder recorder = new Recorder();
        StdHost host = Harness.compile(delayed("flow.delay")).instantiate(recorder);

        host.run();
        host.run();
        assertEquals(List.of("now", "now"), recorder.values());
        host.tick();
        assertEquals(List.of("now", "now", "later", "later"), recorder.values());
        host.tick();
        assertEquals(4, recorder.values().size());
    }

    @Test
    void retriggerableDelayStartsItsWaitAnew() throws GraphCompilationException {
        Recorder recorder = new Recorder();
        StdHost host = Harness.compile(delayed("flow.retriggerable_delay")).instantiate(recorder);

        host.run();
        host.run();
        host.tick();
        assertEquals(List.of("now", "now", "later"), recorder.values());
    }

    @Test
    void delayWaitsItsDuration() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "wait"))
                .node(
                        "wait",
                        NodeInstance.of("flow.delay")
                                .literal("duration", IntNode.valueOf(60))
                                .chain("next", "later"))
                .node("later", NodeInstance.of("test.record").literal("value", StringNode.valueOf("later")))
                .build();
        Recorder recorder = new Recorder();
        StdHost host = Harness.compile(graph).instantiate(recorder);

        host.run();
        host.tick();
        assertEquals(List.of(), recorder.values());
    }

    @Test
    void variablesKeepTheirValueBetweenRuns() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "count", "record"))
                .node(
                        "hits",
                        NodeInstance.of("variable.get")
                                .config("name", StringNode.valueOf("hits"))
                                .config("type", StringNode.valueOf("integer")))
                .node("one", Harness.literal(1))
                .node(
                        "plus",
                        NodeInstance.of("math.add")
                                .input("first", "hits", "value")
                                .input("second", "one", "value"))
                .node(
                        "count",
                        NodeInstance.of("variable.set")
                                .config("name", StringNode.valueOf("hits"))
                                .config("type", StringNode.valueOf("integer"))
                                .input("value", "plus", "result"))
                .node(
                        "read",
                        NodeInstance.of("variable.get")
                                .config("name", StringNode.valueOf("hits"))
                                .config("type", StringNode.valueOf("integer")))
                .node("record", NodeInstance.of("test.record").input("value", "read", "value"))
                .build();

        assertEquals(List.of(1, 2, 3), Harness.run(graph, 3));
    }

    @Test
    void variablesStartEmpty() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record"))
                .node(
                        "name",
                        NodeInstance.of("variable.get")
                                .config("name", StringNode.valueOf("label"))
                                .config("type", StringNode.valueOf("text")))
                .node("record", NodeInstance.of("test.record").input("value", "name", "value"))
                .build();

        assertEquals(List.of(""), Harness.run(graph, 1));
    }

    @Test
    void aVariableNameHoldsOneType() {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record", "other"))
                .node(
                        "number",
                        NodeInstance.of("variable.get")
                                .config("name", StringNode.valueOf("x"))
                                .config("type", StringNode.valueOf("integer")))
                .node(
                        "text",
                        NodeInstance.of("variable.get")
                                .config("name", StringNode.valueOf("x"))
                                .config("type", StringNode.valueOf("text")))
                .node("record", NodeInstance.of("test.record").input("value", "number", "value"))
                .node("other", NodeInstance.of("test.record").input("value", "text", "value"))
                .build();

        GraphCompilationException failure = assertThrows(GraphCompilationException.class, () -> Harness.compile(graph));
        assertTrue(
                failure.getMessage().contains("variable x already holds int values, not String"), failure.getMessage());
    }

    private static NodeInstance.Builder record(String value) {
        return NodeInstance.of("test.record").literal("value", StringNode.valueOf(value));
    }

    @Test
    void doOnceRunsUntilReset() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "once", "every"))
                .node("once", NodeInstance.of("flow.do_once").chain("next", "first"))
                .node("first", record("first"))
                .node(
                        "every",
                        NodeInstance.of("flow.do_n")
                                .literal("count", IntNode.valueOf(2))
                                .chain("next", "count"))
                .node("count", NodeInstance.of("test.record").input("value", "every", "counter"))
                .build();

        assertEquals(List.of("first", 1, 2), Harness.run(graph, 3));
    }

    @Test
    void resetLetsDoOnceRunAgain() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "once", "reset"))
                .node(
                        "once",
                        NodeInstance.of("flow.do_once")
                                .config("name", StringNode.valueOf("intro"))
                                .chain("next", "first"))
                .node("first", record("again"))
                .node("reset", NodeInstance.of("flow.reset_once").config("name", StringNode.valueOf("intro")))
                .build();

        assertEquals(List.of("again", "again"), Harness.run(graph, 2));
    }

    @Test
    void flipFlopAlternates() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "flip"))
                .node("flip", NodeInstance.of("flow.flip_flop").chain("a", "a").chain("b", "b"))
                .node("a", record("a"))
                .node("b", record("b"))
                .build();

        assertEquals(List.of("a", "b", "a"), Harness.run(graph, 3));
    }

    @Test
    void gatesOpenAndClose() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "gate", "toggle"))
                .node("gate", NodeInstance.of("flow.gate").chain("next", "through"))
                .node("through", record("through"))
                .node("toggle", NodeInstance.of("flow.set_gate").config("change", StringNode.valueOf("TOGGLE")))
                .build();

        assertEquals(List.of("through", "through"), Harness.run(graph, 3));
    }

    private static List<Object> selected(NodeInstance.Builder select) throws GraphCompilationException {
        return Harness.run(
                Graph.builder()
                        .node("run", NodeInstance.of("test.run").chain("next", "record"))
                        .node("select", select)
                        .node("record", NodeInstance.of("test.record").input("value", "select", "value"))
                        .build(),
                1);
    }

    @Test
    void selectGivesTheOptionOfTheKey() throws GraphCompilationException {
        for (int key : new int[] {2, 7, 9}) {
            NodeInstance.Builder select = NodeInstance.of("select")
                    .config("key_type", StringNode.valueOf("integer"))
                    .config("type", StringNode.valueOf("text"))
                    .literal("key", IntNode.valueOf(key))
                    .config("key_0", IntNode.valueOf(2))
                    .literal("option_0", StringNode.valueOf("two"))
                    .config("key_3", IntNode.valueOf(7))
                    .literal("option_3", StringNode.valueOf("seven"))
                    .literal("default", StringNode.valueOf("other"));

            String expected = key == 2 ? "two" : key == 7 ? "seven" : "other";
            assertEquals(List.of(expected), selected(select));
        }
    }

    @Test
    void selectByABooleanNeedsNoKeys() throws GraphCompilationException {
        for (boolean key : new boolean[] {true, false}) {
            NodeInstance.Builder select = NodeInstance.of("select")
                    .config("key_type", StringNode.valueOf("boolean"))
                    .config("type", StringNode.valueOf("integer"))
                    .literal("key", BooleanNode.valueOf(key))
                    .literal("option_0", IntNode.valueOf(1))
                    .literal("option_1", IntNode.valueOf(0));

            assertEquals(List.of(key ? 1 : 0), selected(select));
        }
    }

    @Test
    void selectWithoutADefaultGivesTheEmptyValue() throws GraphCompilationException {
        NodeInstance.Builder select = NodeInstance.of("select")
                .config("key_type", StringNode.valueOf("text"))
                .config("type", StringNode.valueOf("integer"))
                .literal("key", StringNode.valueOf("none"))
                .config("key_0", StringNode.valueOf("some"))
                .literal("option_0", IntNode.valueOf(5));

        assertEquals(List.of(0), selected(select));
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
    void reroutesPassValuesAndChainsOn() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "hop"))
                .node("hop", NodeInstance.of("reroute.execution").chain("next", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "via", "value"))
                .node("via", NodeInstance.of("reroute").input("value", "sum", "result"))
                .node(
                        "sum",
                        NodeInstance.of("math.add")
                                .input("first", "two", "value")
                                .input("second", "two", "value"))
                .node("two", Harness.literal(2))
                .build();

        assertEquals(List.of(4), Harness.run(graph, 1));
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
                                .input("first", "elapsed", "elapsed")
                                .input("second", "other", "elapsed"))
                .node("fresh", NodeInstance.of("test.record").input("value", "less", "result"))
                .build();

        assertEquals(List.of(true), Harness.run(graph, 1));
        String source = Harness.compile(graph).source();
        assertEquals(2, source.split("new Timer\\(\\)", -1).length - 1, source);
    }

    @Test
    void branchWritesOnlyTheArmsThatRun() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "branch", "after"))
                .node(
                        "never",
                        NodeInstance.of("logic.greater")
                                .literal("first", DoubleNode.valueOf(1))
                                .literal("second", DoubleNode.valueOf(2)))
                .node(
                        "branch",
                        NodeInstance.of("flow.branch")
                                .input("condition", "never", "result")
                                .chain("false", "record"))
                .node("record", NodeInstance.of("test.record").literal("value", StringNode.valueOf("ran")))
                .node("after", NodeInstance.of("test.record").literal("value", StringNode.valueOf("after")))
                .build();

        String source = Harness.compile(graph).source();
        assertEquals(List.of("ran", "after"), Harness.run(graph, 1));
        assertTrue(source.contains("if (!(1.0d > 2.0d)) {"), source);
        assertFalse(source.contains("else"), source);
        assertFalse(source.contains("return"), source);
    }

    @Test
    void aBranchEndingTheMethodReturnsEarly() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "branch"))
                .node(
                        "never",
                        NodeInstance.of("logic.greater")
                                .literal("first", DoubleNode.valueOf(1))
                                .literal("second", DoubleNode.valueOf(2)))
                .node(
                        "branch",
                        NodeInstance.of("flow.branch")
                                .input("condition", "never", "result")
                                .chain("true", "record"))
                .node("record", NodeInstance.of("test.record").literal("value", StringNode.valueOf("ran")))
                .build();

        String source = Harness.compile(graph).source();
        assertEquals(List.of(), Harness.run(graph, 1));
        assertTrue(source.contains("if (!(1.0d > 2.0d)) return;"), source);
        assertTrue(source.contains("        this.context.record(\"ran\");"), source);
    }

    @Test
    void valuesReadOnceGoStraightIntoTheirReader() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record"))
                .node("abs", NodeInstance.of("math.abs").literal("value", DoubleNode.valueOf(-2)))
                .node("record", NodeInstance.of("test.record").input("value", "abs", "result"))
                .build();

        assertEquals(List.of(2.0), Harness.run(graph, 1));
        assertTrue(Harness.compile(graph).source().contains("record(Math.abs(-2.0d))"));
    }

    @Test
    void valuesReadInNestedBlocksStayLocals() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "branch"))
                .node(
                        "random",
                        NodeInstance.of("function.random_number")
                                .literal("lower", DoubleNode.valueOf(0))
                                .literal("upper", DoubleNode.valueOf(1)))
                .node("abs", NodeInstance.of("math.abs").input("value", "random", "result"))
                .node(
                        "small",
                        NodeInstance.of("logic.less")
                                .input("first", "abs", "result")
                                .literal("second", DoubleNode.valueOf(2)))
                .node(
                        "branch",
                        NodeInstance.of("flow.branch")
                                .input("condition", "small", "result")
                                .chain("true", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "abs", "result"))
                .build();

        String source = Harness.compile(graph).source();
        assertEquals(1, Harness.run(graph, 1).size());
        assertTrue(source.contains("final double abs = Math.abs(nextDouble(0.0d, 1.0d));"), source);
        assertTrue(source.contains("abs < 2.0d"), source);
    }

    @Test
    void statefulValuesStayLocals() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record"))
                .node("cooldown", NodeInstance.of("function.cooldown").literal("duration", DoubleNode.valueOf(1)))
                .node("record", NodeInstance.of("test.record").input("value", "cooldown", "result"))
                .build();

        String source = Harness.compile(graph).source();
        assertEquals(List.of(true, false), Harness.run(graph, 2));
        assertTrue(source.contains("final boolean cooldown = this.cooldown.ready(SECONDS.toMillis(1.0d));"), source);
    }

    @Test
    void integerMathStaysInteger() throws GraphCompilationException {
        assertEquals(5, Harness.evaluate("math.add", Map.of("first", 2, "second", 3)));
        assertEquals(-1, Harness.evaluate("math.subtract", Map.of("first", 2, "second", 3)));
        assertEquals(6, Harness.evaluate("math.multiply", Map.of("first", 2, "second", 3)));
    }

    @Test
    void integerDivisionRoundsTowardZeroAndNeverThrows() throws GraphCompilationException {
        assertEquals(3, Harness.evaluate("math.divide", Map.of("first", 7, "second", 2)));
        assertEquals(-3, Harness.evaluate("math.divide", Map.of("first", -7, "second", 2)));
        assertEquals(0, Harness.evaluate("math.divide", Map.of("first", 7, "second", 0)));
        assertEquals(2, Harness.evaluate("math.modulo_integer", Map.of("first", -1, "second", 3)));
        assertEquals(0, Harness.evaluate("math.modulo_integer", Map.of("first", 5, "second", 0)));
    }
}
