/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.MacroDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.Port;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.Macro;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.toy.ToyContext;
import de.eldoria.graph.toy.ToyHost;
import de.eldoria.graph.toy.ToyNodes;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.StringNode;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroTest {
    private static final ExpressionDefinition BROKEN = new ExpressionDefinition(
            NodeDescriptor.builder("debug.broken", NodeKind.VALUE)
                    .output("value", int.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("undefinedVariable"));
    private static final GraphPlatform PLATFORM = GraphPlatform.builder(ToyHost.class)
            .context(ToyContext.class)
            .define(ToyNodes.ALL)
            .define(BROKEN)
            .classpath(List.of(codeSource()))
            .build();

    private static Path codeSource() {
        try {
            return Path.of(ToyHost.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static NodeInstance.Builder named(String type, String name) {
        return NodeInstance.of(type).config("name", StringNode.valueOf(name));
    }

    private static NodeInstance.Builder number(double value) {
        return NodeInstance.of("value.number").config("value", DoubleNode.valueOf(value));
    }

    private static NodeInstance.Builder print(String node, String port) {
        return NodeInstance.of("debug.print").input("value", node, port);
    }

    private static NodeInstance.Builder text(String value) {
        return NodeInstance.of("value.text").config("value", StringNode.valueOf(value));
    }

    /**
     * {@code double_it(x: number) -> result}
     */
    private static Macro doubleIt() {
        return new Macro(
                "double_it",
                "Double It",
                "Adds a number to itself",
                null,
                Graph.builder()
                        .node("x", named("macro.input.number", "x"))
                        .node(
                                "add",
                                NodeInstance.of("math.add")
                                        .input("a", "x", "value")
                                        .input("b", "x", "value"))
                        .node("out", named("macro.output", "result").input("value", "add", "result"))
                        .build());
    }

    /**
     * Action with exits {@code high} and {@code low}, depending on whether {@code value} is greater than ten.
     */
    private static Macro check() {
        return new Macro(
                "check",
                null,
                null,
                null,
                Graph.builder()
                        .node("start", NodeInstance.of("macro.start").chain("next", "branch"))
                        .node("value", named("macro.input.integer", "value"))
                        .node("ten", number(10))
                        .node(
                                "greater",
                                NodeInstance.of("logic.greater")
                                        .input("a", "value", "value")
                                        .input("b", "ten", "value"))
                        .node(
                                "branch",
                                NodeInstance.of("flow.branch")
                                        .input("condition", "greater", "result")
                                        .chain("true", "high")
                                        .chain("false", "low"))
                        .node("high", named("macro.end", "high"))
                        .node("low", named("macro.end", "low"))
                        .build());
    }

    private static Macro quadruple() {
        return new Macro(
                "quadruple",
                null,
                null,
                null,
                Graph.builder()
                        .node("x", named("macro.input.number", "x"))
                        .node("first", NodeInstance.of("macro.double_it").input("x", "x", "value"))
                        .node("second", NodeInstance.of("macro.double_it").input("x", "first", "result"))
                        .node("out", named("macro.output", "result").input("value", "second", "result"))
                        .build());
    }

    private static Macro counting() {
        return new Macro(
                "counting",
                null,
                null,
                null,
                Graph.builder()
                        .node("counter", NodeInstance.of("state.counter"))
                        .node("out", named("macro.output", "count").input("value", "counter", "count"))
                        .build());
    }

    private static Macro brokenInside() {
        return new Macro(
                "broken_inside",
                null,
                null,
                null,
                Graph.builder()
                        .node("oops", NodeInstance.of("debug.broken"))
                        .node("out", named("macro.output", "value").input("value", "oops", "value"))
                        .build());
    }

    private static MacroLibrary library(Macro... macros) {
        return MacroLibrary.compile(PLATFORM, List.of(macros));
    }

    private static CompiledGraph<ToyHost> compile(Graph graph, MacroLibrary library) throws GraphCompilationException {
        return new GraphCompiler<>(PLATFORM, ToyHost.class, MacroTest.class.getClassLoader(), library)
                .compile(graph, ClassName.get("generated.macro", "Behaviour"));
    }

    private static List<String> run(Graph graph, MacroLibrary library, Consumer<ToyHost> calls)
            throws GraphCompilationException {
        ToyContext context = new ToyContext();
        calls.accept(compile(graph, library).instantiate(context));
        return context.printed();
    }

    private static GraphProblem problem(GraphCompilationException exception, String node, String port) {
        return exception.problems().stream()
                .filter(problem -> Objects.equals(problem.node(), node) && Objects.equals(problem.port(), port))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("No problem on %s.%s:\n%s".formatted(node, port, exception.getMessage())));
    }

    @Test
    void descriptorsComeFromBoundaryNodes() {
        MacroLibrary library = library(doubleIt(), check());

        MacroDefinition doubled = library.definitions().get("macro.double_it");
        assertEquals(NodeKind.PURE, doubled.descriptor().kind());
        assertEquals("Double It", doubled.descriptor().name());
        assertEquals("macro", doubled.descriptor().category());
        assertEquals(
                List.of("x"),
                doubled.descriptor().inputs().stream().map(Port::id).toList());
        assertEquals(
                PortType.of(double.class),
                doubled.descriptor().output("result").orElseThrow().type());

        MacroDefinition check = library.definitions().get("macro.check");
        assertEquals(NodeKind.ACTION, check.descriptor().kind());
        assertEquals(
                PortType.of(int.class),
                check.descriptor().input("value").orElseThrow().type());
        assertEquals(
                List.of("high", "low"),
                check.descriptor().executions().stream().map(port -> port.id()).toList());
        assertTrue(library.problems().isEmpty(), library.problems().toString());
    }

    @Test
    void pureMacro() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("four", number(4))
                .node("twice", NodeInstance.of("macro.double_it").input("x", "four", "value"))
                .node("print", print("twice", "result"))
                .build();

        assertEquals(List.of("8.0"), run(graph, library(doubleIt()), ToyHost::onStart));
    }

    @Test
    void macroInputsTakeTypedInValues() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("twice", NodeInstance.of("macro.double_it").literal("x", DoubleNode.valueOf(4.5)))
                .node("print", print("twice", "result"))
                .build();

        assertEquals(List.of("9.0"), run(graph, library(doubleIt()), ToyHost::onStart));
    }

    @Test
    void actionMacroWithExits() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("score", NodeInstance.of("entry.score").chain("next", "check", "done"))
                .node(
                        "check",
                        NodeInstance.of("macro.check")
                                .input("value", "score", "points")
                                .chain("high", "high")
                                .chain("low", "low"))
                .node("high", print("high_text", "value"))
                .node("low", print("low_text", "value"))
                .node("done", print("score", "player"))
                .node("high_text", text("high"))
                .node("low_text", text("low"))
                .build();

        assertEquals(List.of("high", "a", "low", "b"), run(graph, library(check()), host -> {
            host.onScore(20, "a");
            host.onScore(5, "b");
        }));
    }

    @Test
    void nestedMacros() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("three", number(3))
                .node("times_four", NodeInstance.of("macro.quadruple").input("x", "three", "value"))
                .node("print", print("times_four", "result"))
                .build();

        assertEquals(List.of("12.0"), run(graph, library(quadruple(), doubleIt()), ToyHost::onStart));
    }

    @Test
    void everyUseHasItsOwnState() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "a", "b", "c"))
                .node("first", NodeInstance.of("macro.counting"))
                .node("second", NodeInstance.of("macro.counting"))
                .node("a", print("first", "count"))
                .node("b", print("second", "count"))
                .node("c", print("first", "count"))
                .build();

        assertEquals(List.of("1", "1", "1", "2", "2", "2"), run(graph, library(counting()), host -> {
            host.onStart();
            host.onStart();
        }));
    }

    @Test
    void storedGraphKeepsTheMacroNode() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("four", number(4))
                .node("twice", NodeInstance.of("macro.double_it").input("x", "four", "value"))
                .node("print", print("twice", "result"))
                .build();

        compile(graph, library(doubleIt()));

        assertEquals("macro.double_it", graph.nodes().get("twice").type());
    }

    @Test
    void inputsAreTypeChecked() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("word", text("four"))
                .node("twice", NodeInstance.of("macro.double_it").input("x", "word", "value"))
                .node("print", print("twice", "result"))
                .build();

        GraphCompilationException exception =
                assertThrows(GraphCompilationException.class, () -> compile(graph, library(doubleIt())));

        GraphProblem problem = problem(exception, "twice", "x");
        assertTrue(problem.message().contains("Expected Number but word provides Text"), problem.message());
        assertTrue(problem.trace().isEmpty());
    }

    @Test
    void scopeErrorsLandOnTheMacroInput() {
        Graph graph = Graph.builder()
                .node("score", NodeInstance.of("entry.score"))
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("check", NodeInstance.of("macro.check").input("value", "score", "points"))
                .node("twice", NodeInstance.of("macro.double_it").input("x", "score", "points"))
                .node("print", print("twice", "result"))
                .build();

        GraphCompilationException exception =
                assertThrows(GraphCompilationException.class, () -> compile(graph, library(doubleIt(), check())));

        assertTrue(problem(exception, "twice", "x").message().contains("only available in the chain it starts"));
    }

    @Test
    void compilerErrorsAreTracedIntoTheMacro() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("use", NodeInstance.of("macro.broken_inside"))
                .node("print", print("use", "value"))
                .build();

        GraphCompilationException exception =
                assertThrows(GraphCompilationException.class, () -> compile(graph, library(brokenInside())));

        GraphProblem problem = problem(exception, "use", null);
        assertEquals(List.of(new MacroFrame("broken_inside", "oops")), problem.trace());
        assertTrue(problem.toString().contains("use > broken_inside:oops"), problem.toString());
    }

    @Test
    void nestedProblemsCarryTheWholeTrace() {
        Macro wrapper = new Macro(
                "wrapper",
                null,
                null,
                null,
                Graph.builder()
                        .node("inner", NodeInstance.of("macro.broken_inside"))
                        .node("out", named("macro.output", "value").input("value", "inner", "value"))
                        .build());
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("use", NodeInstance.of("macro.wrapper"))
                .node("print", print("use", "value"))
                .build();

        GraphCompilationException exception =
                assertThrows(GraphCompilationException.class, () -> compile(graph, library(wrapper, brokenInside())));

        assertEquals(
                List.of(new MacroFrame("wrapper", "inner"), new MacroFrame("broken_inside", "oops")),
                problem(exception, "use", null).trace());
    }

    @Test
    void useMistakesAreReportedOnTheUse() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "twice", "print"))
                .node("twice", NodeInstance.of("macro.double_it").input("y", "start", "next"))
                .node("print", print("twice", "missing"))
                .build();

        GraphCompilationException exception =
                assertThrows(GraphCompilationException.class, () -> compile(graph, library(doubleIt())));

        assertTrue(problem(exception, "twice", "x").message().contains("not connected"));
        assertTrue(problem(exception, "twice", "y").message().contains("no input y"));
        assertTrue(problem(exception, "start", "next").message().contains("cannot be chained"));
        assertTrue(problem(exception, "print", "value").message().contains("no output missing"));
    }

    @Test
    void brokenMacrosAreLeftOutAndReported() {
        Macro missingInput = new Macro(
                "missing_input",
                null,
                null,
                null,
                Graph.builder()
                        .node("add", NodeInstance.of("math.add"))
                        .node("out", named("macro.output", "result").input("value", "add", "result"))
                        .build());
        Macro unnamed = new Macro(
                "unnamed",
                null,
                null,
                null,
                Graph.builder()
                        .node("x", NodeInstance.of("macro.input.number"))
                        .node("out", NodeInstance.of("macro.output").input("value", "x", "value"))
                        .build());
        Macro empty = new Macro("empty", null, null, null, Graph.builder().build());
        Macro withEntry = new Macro(
                "with_entry",
                null,
                null,
                null,
                Graph.builder()
                        .node("start", NodeInstance.of("entry.start"))
                        .node("x", named("macro.input.number", "x"))
                        .node("out", named("macro.output", "x").input("value", "x", "value"))
                        .build());
        Macro uses = new Macro(
                "uses_broken",
                null,
                null,
                null,
                Graph.builder()
                        .node("inner", NodeInstance.of("macro.missing_input"))
                        .node("out", named("macro.output", "result").input("value", "inner", "result"))
                        .build());

        MacroLibrary library = library(
                missingInput,
                unnamed,
                empty,
                withEntry,
                uses,
                doubleIt(),
                new Macro("input", null, null, null, Graph.builder().build()));

        assertEquals(
                List.of("macro.double_it"),
                new ArrayList<>(library.definitions().keySet()));
        assertTrue(library.problems().get("missing_input").stream()
                .anyMatch(problem -> "add".equals(problem.node()) && "b".equals(problem.port())));
        assertTrue(library.problems().get("unnamed").stream()
                .anyMatch(problem -> "x".equals(problem.node()) && "name".equals(problem.port())));
        assertTrue(library.problems().get("empty").stream()
                .anyMatch(problem -> problem.message().contains("needs a start or at least one output")));
        assertTrue(library.problems().get("with_entry").stream()
                .anyMatch(problem ->
                        "start".equals(problem.node()) && problem.message().contains("Unknown node type")));
        assertTrue(library.problems().get("uses_broken").stream()
                .anyMatch(problem -> problem.message().contains("missing_input, which has errors")));
        assertTrue(library.problems().get("input").stream()
                .anyMatch(problem -> problem.message().contains("Invalid macro id")));
    }

    @Test
    void cyclesAreRejected() {
        Macro a = new Macro(
                "a",
                null,
                null,
                null,
                Graph.builder()
                        .node("b", NodeInstance.of("macro.b"))
                        .node("out", named("macro.output", "value").input("value", "b", "value"))
                        .build());
        Macro b = new Macro(
                "b",
                null,
                null,
                null,
                Graph.builder()
                        .node("a", NodeInstance.of("macro.a"))
                        .node("out", named("macro.output", "value").input("value", "a", "value"))
                        .build());

        MacroLibrary library = library(a, b);

        assertTrue(library.definitions().isEmpty());
        assertTrue(library.problems().get("a").getFirst().message().contains("cycle"));
        assertTrue(library.problems().get("b").getFirst().message().contains("cycle"));
    }

    @Test
    void boundaryNodesAreNotPartOfThePlatform() {
        assertFalse(PLATFORM.node("macro.start").isPresent());
    }
}
