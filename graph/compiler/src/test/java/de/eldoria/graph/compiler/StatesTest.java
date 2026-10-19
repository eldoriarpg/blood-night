/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.model.State;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.toy.ToyContext;
import de.eldoria.graph.toy.ToyHost;
import de.eldoria.graph.toy.ToyNodes;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.StringNode;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatesTest {
    private static final ClassName NAME = ClassName.get("generated.toy", "States");

    private static CompiledGraph<ToyHost> compile(Graph graph) throws GraphCompilationException {
        GraphPlatform platform = GraphPlatform.builder(ToyHost.class)
                .context(ToyContext.class)
                .define(ToyNodes.ALL)
                .classpath(List.of(codeSource()))
                .build();
        return new GraphCompiler<>(platform, ToyHost.class, StatesTest.class.getClassLoader()).compile(graph, NAME);
    }

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

    /**
     * Adds a node printing a text, and the text it prints.
     */
    private static Graph.Builder say(Graph.Builder graph, String id, String state, String text, String... next) {
        return graph.node(
                        id + "_text",
                        NodeInstance.of("value.text")
                                .config("value", StringNode.valueOf(text))
                                .state(state))
                .node(
                        id,
                        NodeInstance.of("debug.print")
                                .input("value", id + "_text", "value")
                                .chain("next", next)
                                .state(state));
    }

    private static NodeInstance.Builder goTo(String state, String target) {
        return NodeInstance.of("state.go_to")
                .config("state", StringNode.valueOf(target))
                .state(state);
    }

    private static Graph.Builder fight() {
        Graph.Builder graph = Graph.builder()
                .state(new State("fight", null, true))
                .state(new State("calm", "fight", true))
                .state(new State("angry", "fight", false))
                .state(new State("casting", null, false))
                .node(
                        "calm_start",
                        NodeInstance.of("entry.start").chain("next", "calm_say").state("calm"))
                .node("calm_go", goTo("calm", "angry"))
                .node(
                        "angry_start",
                        NodeInstance.of("entry.start")
                                .chain("next", "angry_say")
                                .state("angry"))
                .node("angry_go", goTo("angry", "casting"))
                .node(
                        "fight_enter",
                        NodeInstance.of("state.enter").chain("next", "fight_hi").state("fight"))
                .node(
                        "fight_exit",
                        NodeInstance.of("state.exit").chain("next", "fight_bye").state("fight"))
                .node(
                        "calm_enter",
                        NodeInstance.of("state.enter").chain("next", "calm_hi").state("calm"))
                .node(
                        "calm_exit",
                        NodeInstance.of("state.exit").chain("next", "calm_bye").state("calm"))
                .node(
                        "angry_enter",
                        NodeInstance.of("state.enter").chain("next", "angry_hi").state("angry"))
                .node(
                        "casting_enter",
                        NodeInstance.of("state.enter")
                                .chain("next", "casting_hi")
                                .state("casting"));
        say(graph, "calm_say", "calm", "calm", "calm_go");
        say(graph, "angry_say", "angry", "angry", "angry_go");
        say(graph, "fight_hi", "fight", "enter fight");
        say(graph, "fight_bye", "fight", "exit fight");
        say(graph, "calm_hi", "calm", "enter calm");
        say(graph, "calm_bye", "calm", "exit calm");
        say(graph, "angry_hi", "angry", "enter angry");
        say(graph, "casting_hi", "casting", "enter casting");
        return graph;
    }

    @Test
    void movingBetweenNestedStatesRunsOnlyTheExitsAndEntersBetweenThem() throws GraphCompilationException {
        ToyContext context = new ToyContext();
        ToyHost host = compile(fight().build()).instantiate(context);

        host.onStart();
        assertEquals(List.of("enter fight", "enter calm", "calm", "exit calm", "enter angry"), context.printed());

        host.onStart();
        assertEquals(
                List.of(
                        "enter fight",
                        "enter calm",
                        "calm",
                        "exit calm",
                        "enter angry",
                        "angry",
                        "exit fight",
                        "enter casting"),
                context.printed());
    }

    @Test
    void aStateThatSaysNoIsNotEntered() throws GraphCompilationException {
        Graph graph = fight().node("one", NodeInstance.of("value.number").config("value", DoubleNode.valueOf(1)))
                .node("two", NodeInstance.of("value.number").config("value", DoubleNode.valueOf(2)))
                .node(
                        "no",
                        NodeInstance.of("logic.greater")
                                .input("a", "one", "value")
                                .input("b", "two", "value"))
                .node(
                        "casting_if",
                        NodeInstance.of("state.can_enter")
                                .input("allowed", "no", "result")
                                .state("casting"))
                .build();
        ToyContext context = new ToyContext();
        ToyHost host = compile(graph).instantiate(context);

        host.onStart();
        host.onStart();
        host.onStart();
        assertEquals(
                List.of("enter fight", "enter calm", "calm", "exit calm", "enter angry", "angry", "angry"),
                context.printed());
    }

    @Test
    void statesAreChecked() {
        Graph graph = Graph.builder()
                .state(new State("a", null, true))
                .state(new State("b", null, true))
                .node(
                        "start",
                        NodeInstance.of("entry.start").chain("next", "go").state("a"))
                .node("go", goTo("b", "nowhere"))
                .node("lost", NodeInstance.of("entry.start").state("missing").chain("next"))
                .build();
        List<GraphProblem> problems = assertThrows(GraphCompilationException.class, () -> compile(graph))
                .problems();
        String all = problems.toString();
        assertTrue(all.contains("the top level needs exactly one initial state, has 2"), all);
        assertTrue(all.contains("Chained from a node of another state"), all);
        assertTrue(all.contains("No state nowhere"), all);
        assertTrue(all.contains("No state missing"), all);
    }
}
