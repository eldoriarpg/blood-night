package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.toy.ToyContext;
import de.eldoria.graph.toy.ToyHost;
import de.eldoria.graph.toy.ToyNodes;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphCompilerTest {
    private static final ClassName NAME = ClassName.get("generated.toy", "Behaviour");

    private static GraphPlatform platform(ExpressionDefinition... extra) {
        return GraphPlatform.builder(ToyHost.class)
                .context(ToyContext.class)
                .define(ToyNodes.ALL)
                .define(extra)
                .classpath(List.of(codeSource()))
                .build();
    }

    private static Path codeSource() {
        try {
            return Path.of(ToyHost.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static CompiledGraph<ToyHost> compile(Graph graph, ExpressionDefinition... extra) throws GraphCompilationException {
        return new GraphCompiler<>(platform(extra), ToyHost.class, GraphCompilerTest.class.getClassLoader()).compile(graph, NAME);
    }

    private static List<String> run(Graph graph, Consumer<ToyHost> calls) throws GraphCompilationException {
        ToyContext context = new ToyContext();
        calls.accept(compile(graph).instantiate(context));
        return context.printed();
    }

    private static List<GraphProblem> problems(Graph graph, ExpressionDefinition... extra) {
        return assertThrows(GraphCompilationException.class, () -> compile(graph, extra)).problems();
    }

    private static void assertProblem(List<GraphProblem> problems, String node, String port, String messagePart) {
        assertTrue(problems.stream().anyMatch(problem -> problem.severity() == GraphProblem.Severity.ERROR
                        && Objects.equals(problem.node(), node)
                        && Objects.equals(problem.port(), port)
                        && problem.message().contains(messagePart)),
                () -> "Expected error on %s.%s containing '%s', got:\n%s".formatted(node, port, messagePart,
                        String.join("\n", problems.stream().map(GraphProblem::toString).toList())));
    }

    private static NodeInstance.Builder number(double value) {
        return NodeInstance.of("value.number").config("value", DoubleNode.valueOf(value));
    }

    private static NodeInstance.Builder print(String node, String port) {
        return NodeInstance.of("debug.print").input("value", node, port);
    }

    @Test
    void computesAndPrints() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("two", number(2))
                .node("three", number(3))
                .node("add", NodeInstance.of("math.add").input("a", "two", "value").input("b", "three", "value"))
                .node("print", print("add", "result"))
                .build();

        assertEquals(List.of("5.0"), run(graph, ToyHost::onStart));
    }

    @Test
    void entryOutputsComeFromParameters() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("score", NodeInstance.of("entry.score").chain("next", "points", "player"))
                .node("points", print("score", "points"))
                .node("player", print("score", "player"))
                .build();

        assertEquals(List.of("7", "Lilly"), run(graph, host -> host.onScore(7, "Lilly")));
    }

    @Test
    void chainsRunInOrderAndNest() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "first", "third"))
                .node("first", print("one", "value").chain("next", "second"))
                .node("second", print("two", "value"))
                .node("third", print("three", "value"))
                .node("one", number(1))
                .node("two", number(2))
                .node("three", number(3))
                .build();

        assertEquals(List.of("1.0", "2.0", "3.0"), run(graph, ToyHost::onStart));
    }

    @Test
    void branches() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("score", NodeInstance.of("entry.score").chain("next", "branch"))
                .node("ten", number(10))
                .node("greater", NodeInstance.of("logic.greater").input("a", "score", "points").input("b", "ten", "value"))
                .node("branch", NodeInstance.of("flow.branch").input("condition", "greater", "result")
                        .chain("true", "high").chain("false", "low").chain("next", "done"))
                .node("high", print("high_text", "value"))
                .node("low", print("low_text", "value"))
                .node("done", print("score", "player"))
                .node("high_text", NodeInstance.of("value.text").config("value", StringNode.valueOf("high")))
                .node("low_text", NodeInstance.of("value.text").config("value", StringNode.valueOf("low")))
                .build();

        CompiledGraph<ToyHost> compiled = compile(graph);
        ToyContext context = new ToyContext();
        ToyHost host = compiled.instantiate(context);
        host.onScore(20, "a");
        host.onScore(5, "b");

        assertEquals(List.of("high", "a", "low", "b"), context.printed());
    }

    @Test
    void pureNodesAreEvaluatedOncePerScope() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "first", "second"))
                .node("counter", NodeInstance.of("state.counter"))
                .node("first", print("counter", "count"))
                .node("second", print("counter", "count"))
                .build();

        assertEquals(List.of("1", "1", "2", "2"), run(graph, host -> {
            host.onStart();
            host.onStart();
        }));
    }

    @Test
    void stateBelongsToTheInstance() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("counter", NodeInstance.of("state.counter"))
                .node("print", print("counter", "count"))
                .build();
        CompiledGraph<ToyHost> compiled = compile(graph);
        ToyContext first = new ToyContext();
        ToyContext second = new ToyContext();
        ToyHost a = compiled.instantiate(first);
        ToyHost b = compiled.instantiate(second);

        a.onStart();
        a.onStart();
        b.onStart();

        assertEquals(List.of("1", "2"), first.printed());
        assertEquals(List.of("1"), second.printed());
    }

    @Test
    void loopBodiesRecomputeAndSeeTheirVariable() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "before", "repeat"))
                .node("counter", NodeInstance.of("state.counter"))
                .node("before", print("counter", "count"))
                .node("repeat", NodeInstance.of("flow.repeat").config("times", IntNode.valueOf(3))
                        .chain("body", "index", "inside").chain("next", "after"))
                .node("index", print("repeat", "index"))
                .node("inside", print("counter", "count"))
                .node("after", print("counter", "count"))
                .build();

        assertEquals(List.of("1", "0", "2", "1", "3", "2", "4", "1"), run(graph, ToyHost::onStart));
    }

    @Test
    void typeVariablesAreInferred() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("score", NodeInstance.of("entry.score").chain("next", "print"))
                .node("ten", number(10))
                .node("greater", NodeInstance.of("logic.greater").input("a", "score", "points").input("b", "ten", "value"))
                .node("pick", NodeInstance.of("logic.pick").input("condition", "greater", "result")
                        .input("when_true", "score", "player").input("when_false", "nobody", "value"))
                .node("nobody", NodeInstance.of("value.text").config("value", StringNode.valueOf("nobody")))
                .node("print", print("pick", "result"))
                .build();

        assertEquals(List.of("nobody", "Lilly"), run(graph, host -> {
            host.onScore(1, "Lilly");
            host.onScore(11, "Lilly");
        }));
    }

    @Test
    void emptyBodiesForUncoveredAbstractMethods() throws GraphCompilationException {
        Graph graph = Graph.builder().node("start", NodeInstance.of("entry.start")).build();

        ToyHost host = compile(graph).instantiate(new ToyContext());
        host.onStop();

        assertNotNull(host);
    }

    @Test
    void graphIdsNeverReachTheSource() throws GraphCompilationException {
        String hostile = "x\"); System.exit(1); /*\n*/ //";
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", hostile))
                .node(hostile, print("start'\n", "value"))
                .node("start'\n", number(4))
                .build();

        CompiledGraph<ToyHost> compiled = compile(graph);
        String source = compiled.source().toString();

        assertFalse(source.contains("exit"), source);
        ToyContext context = new ToyContext();
        compiled.instantiate(context).onStart();
        assertEquals(List.of("4.0"), context.printed());
    }

    @Test
    void entryOutputsOutsideTheirChainAreRejected() {
        Graph graph = Graph.builder()
                .node("score", NodeInstance.of("entry.score"))
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("print", print("score", "player"))
                .build();

        assertProblem(problems(graph), "print", "value", "only available in the chain it starts");
    }

    @Test
    void loopVariablesAfterTheLoopAreRejected() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "repeat"))
                .node("repeat", NodeInstance.of("flow.repeat").chain("next", "after"))
                .node("after", print("repeat", "index"))
                .build();

        assertProblem(problems(graph), "after", "value", "only available in the code repeat runs itself");
    }

    @Test
    void validationErrorsAreLocated() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print", "add", "missing").chain("later", "print"))
                .node("unknown", NodeInstance.of("math.divide"))
                .node("add", NodeInstance.of("math.add").input("a", "text", "value").input("c", "text", "value"))
                .node("text", NodeInstance.of("value.text"))
                .node("bad_number", NodeInstance.of("value.number").config("value", StringNode.valueOf("five")).config("unit", StringNode.valueOf("m")))
                .node("print", print("ghost", "value"))
                .node("print_twice", print("bad_number", "nothing"))
                .node("loop_a", NodeInstance.of("math.add").input("a", "loop_b", "result").input("b", "loop_b", "result"))
                .node("loop_b", NodeInstance.of("math.add").input("a", "loop_a", "result").input("b", "loop_a", "result"))
                .build();

        List<GraphProblem> problems = problems(graph);

        assertProblem(problems, "unknown", null, "Unknown node type math.divide");
        assertProblem(problems, "start", "later", "Unknown execution port");
        assertProblem(problems, "start", "next", "Node add cannot be chained");
        assertProblem(problems, "start", "next", "Chains missing node missing");
        assertProblem(problems, "add", "c", "Unknown input");
        assertProblem(problems, "add", "b", "Required input is not connected");
        assertProblem(problems, "add", "a", "Expected Number but text provides Text");
        assertProblem(problems, "text", "value", "Required config value is missing");
        assertProblem(problems, "bad_number", "value", "Not a valid double");
        assertProblem(problems, "bad_number", "unit", "Unknown config field");
        assertProblem(problems, "print", "value", "Connected to missing node ghost");
        assertProblem(problems, "print_twice", "value", "Node bad_number has no output nothing");
        assertTrue(problems.stream().anyMatch(problem -> problem.message().startsWith("Data edges form a cycle")));
    }

    @Test
    void chainingTwiceIsRejected() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "a"))
                .node("score", NodeInstance.of("entry.score").chain("next", "a"))
                .node("a", NodeInstance.of("flow.repeat"))
                .build();

        assertProblem(problems(graph), "a", null, "Chained more than once");
    }

    @Test
    void executionLoopsAreRejected() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start"))
                .node("b", NodeInstance.of("flow.repeat").chain("body", "c"))
                .node("c", NodeInstance.of("flow.repeat").chain("body", "b"))
                .build();

        List<GraphProblem> problems = problems(graph);

        assertProblem(problems, "b", null, "loops back onto itself");
        assertProblem(problems, "c", null, "loops back onto itself");
    }

    @Test
    void conflictingTypeVariablesAreRejected() {
        Graph graph = Graph.builder()
                .node("score", NodeInstance.of("entry.score").chain("next", "print"))
                .node("yes", NodeInstance.of("logic.greater").input("a", "score", "points").input("b", "score", "points"))
                .node("pick", NodeInstance.of("logic.pick").input("condition", "yes", "result")
                        .input("when_true", "score", "player").input("when_false", "score", "points"))
                .node("print", print("pick", "result"))
                .build();

        assertProblem(problems(graph), "pick", "when_false", "Conflicting types for T");
    }

    @Test
    void unreachableStatementsOnlyWarn() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start"))
                .node("orphan", print("one", "value"))
                .node("one", number(1))
                .build();

        List<GraphProblem> warnings = compile(graph).warnings();

        assertEquals(1, warnings.size());
        assertEquals("orphan", warnings.getFirst().node());
        assertEquals(GraphProblem.Severity.WARNING, warnings.getFirst().severity());
    }

    @Test
    void compilerErrorsPointAtTheFaultyNode() {
        ExpressionDefinition broken = new ExpressionDefinition(
                NodeDescriptor.builder("debug.broken", NodeKind.VALUE).output("value", int.class).build(),
                (port, ctx) -> CodeBlock.of("undefinedVariable"));
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("print", print("oops", "value"))
                .node("oops", NodeInstance.of("debug.broken"))
                .build();

        GraphCompilationException exception = assertThrows(GraphCompilationException.class, () -> compile(graph, broken));

        assertProblem(exception.problems(), "oops", null, "undefinedVariable");
        assertTrue(exception.source().orElseThrow().contains("undefinedVariable"));
    }
}
