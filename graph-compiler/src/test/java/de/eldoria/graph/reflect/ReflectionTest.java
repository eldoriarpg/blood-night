/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import com.palantir.javapoet.ClassName;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphCompiler;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.Port;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.toy.ToyContext;
import de.eldoria.graph.toy.ToyEvent;
import de.eldoria.graph.toy.ToyFluent;
import de.eldoria.graph.toy.ToyFluentApi;
import de.eldoria.graph.toy.ToyHost;
import de.eldoria.graph.toy.ToyNodes;
import de.eldoria.graph.toy.ToyPlayer;
import de.eldoria.graph.toy.ToyPosition;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionTest {
    private static final ReflectionRules RULES =
            ReflectionRules.builder().excludeMethods("getHandlers").build();

    private static List<String> ids(List<Port> ports) {
        return ports.stream().map(Port::id).toList();
    }

    private static NodeDefinition node(List<NodeDefinition> nodes, String id) {
        return nodes.stream().filter(node -> node.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void gettersFollowTheRules() {
        assertEquals(
                List.of("friends", "name", "nickname", "online", "score", "url_string"),
                Properties.getters(ToyPlayer.class, RULES).stream()
                        .map(Properties.Getter::id)
                        .toList());
        assertTrue(
                Properties.getters(
                                ToyPlayer.class,
                                ReflectionRules.builder()
                                        .excludeDeprecated(false)
                                        .build())
                        .stream()
                        .anyMatch(getter -> getter.id().equals("level")));
    }

    @Test
    void settersDisambiguateOverloads() {
        List<Properties.Setter> setters = Properties.setters(ToyPlayer.class, RULES);

        assertEquals(
                List.of("set_nickname", "set_nickname_string", "set_online", "set_score"),
                setters.stream().map(Properties.Setter::id).toList());
        assertEquals(PortType.of(CharSequence.class), setters.getFirst().type());
    }

    @Test
    void recordComponentsAreGetters() {
        NodeDefinition get =
                ReflectedType.of(ToyPosition.class, "position").build(RULES).getFirst();

        assertEquals(List.of("x", "y"), ids(get.descriptor().outputs()));
    }

    @Test
    void fluentAccessorsAreOptIn() {
        assertTrue(ReflectedType.of(ToyFluent.class, "fluent").build(RULES).isEmpty());
    }

    @Test
    void fluentAccessorsMatchFieldsOrPairs() {
        List<NodeDefinition> nodes = ReflectedType.of(ToyFluent.class, "fluent")
                .fluent()
                .accessors("computed")
                .build(RULES);

        assertEquals(
                List.of("computed", "label", "level", "name"),
                ids(nodes.getFirst().descriptor().outputs()));
        assertEquals(
                List.of("fluent.get", "fluent.set_label", "fluent.set_name", "fluent.set"),
                nodes.stream().map(NodeDefinition::id).toList());
    }

    @Test
    void fluentAccessorsOnInterfacesNeedPairs() {
        List<NodeDefinition> nodes =
                ReflectedType.of(ToyFluentApi.class, "api").fluent().build(RULES);
        List<NodeDefinition> manual = ReflectedType.of(ToyFluentApi.class, "api")
                .fluent()
                .accessors("count")
                .build(RULES);

        assertEquals(List.of("title"), ids(nodes.getFirst().descriptor().outputs()));
        assertEquals(
                List.of("count", "title"), ids(manual.getFirst().descriptor().outputs()));
        assertEquals(
                List.of("api.get", "api.set_title", "api.set"),
                nodes.stream().map(NodeDefinition::id).toList());
    }

    @Test
    void recordsCanBeCreatedFromTheirComponents() {
        NodeDefinition create =
                node(ReflectedType.of(ToyPosition.class, "position").build(RULES), "position.create");

        assertEquals(List.of("x", "y"), ids(create.descriptor().inputs()));
        assertEquals(
                PortType.of(ToyPosition.class),
                create.descriptor().output("result").orElseThrow().type());
        assertTrue(ReflectedType.of(ToyPosition.class, "position").recordConstructor(false).build(RULES).stream()
                .noneMatch(node -> node.id().equals("position.create")));
    }

    @Test
    void constructorsAndFactoriesAreOptIn() {
        List<NodeDefinition> plain = ReflectedType.of(ToyPlayer.class, "player").build(RULES);
        List<NodeDefinition> creating = ReflectedType.of(ToyPlayer.class, "player")
                .constructor("create", Param.of("name", String.class), Param.of("score", int.class))
                .factory("fallback", "getDefault")
                .build(RULES);

        assertTrue(plain.stream().noneMatch(node -> node.id().equals("player.create")));
        assertEquals(
                List.of("name", "score"),
                ids(node(creating, "player.create").descriptor().inputs()));
        assertTrue(node(creating, "player.fallback").descriptor().inputs().isEmpty());
        assertEquals(
                "Create ToyPlayer", node(creating, "player.create").descriptor().name());
    }

    @Test
    void unusableConstructorsAndFactoriesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyPlayer.class, "player")
                .constructor("create", Param.of("name", String.class))
                .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyFluentApi.class, "api")
                .constructor("create")
                .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyPlayer.class, "player")
                .factory("name", "getName")
                .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyPlayer.class, "player")
                .factory("load", "load", Param.of("name", String.class))
                .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> Param.of("Not An Id", int.class));
    }

    @Test
    void typeFilterAppliesToPorts() {
        ReflectionRules noLists = ReflectionRules.builder()
                .acceptTypes(type ->
                        !(type instanceof PortType.Concrete concrete && List.class.isAssignableFrom(concrete.raw())))
                .build();

        assertFalse(Properties.getters(ToyPlayer.class, noLists).stream()
                .anyMatch(getter -> getter.id().equals("friends")));
    }

    @Test
    void snakeCase() {
        assertEquals("final_damage", Names.snakeCase("FinalDamage"));
        assertEquals("uuid_string", Names.snakeCase("UUIDString"));
        assertEquals("url", Names.snakeCase("URL"));
        assertEquals("entity_damage_event", Names.snakeCase("EntityDamageEvent"));
        assertEquals("x2_value", Names.snakeCase("X2Value"));
        assertEquals("class_", Names.snakeCase("Class"));
    }

    @Test
    void typeNodes() {
        List<NodeDefinition> nodes = ReflectedType.of(ToyPlayer.class, "toy.player")
                .name("Player")
                .exclude("url_string", "nickname")
                .rename("score", "Points")
                .build(RULES);

        assertEquals(
                List.of("toy.player.get", "toy.player.set_online", "toy.player.set_score", "toy.player.set"),
                nodes.stream().map(NodeDefinition::id).toList());
        NodeDefinition get = nodes.getFirst();
        assertEquals(NodeKind.PURE, get.descriptor().kind());
        assertEquals("Get Player", get.descriptor().name());
        assertEquals("toy", get.descriptor().category());
        assertEquals(List.of("target"), ids(get.descriptor().inputs()));
        assertEquals(
                List.of("friends", "name", "online", "score"),
                ids(get.descriptor().outputs()));
        assertEquals("Points", get.descriptor().output("score").orElseThrow().name());
        assertEquals(
                PortType.parameterized(List.class, PortType.of(String.class)),
                get.descriptor().output("friends").orElseThrow().type());

        NodeDefinition setScore = node(nodes, "toy.player.set_score");
        assertEquals(NodeKind.ACTION, setScore.descriptor().kind());
        assertEquals(List.of("target", "value"), ids(setScore.descriptor().inputs()));
        assertEquals(
                PortType.of(int.class),
                setScore.descriptor().input("value").orElseThrow().type());
        assertEquals("Set Player Points", setScore.descriptor().name());

        NodeDefinition modify = node(nodes, "toy.player.set");
        assertEquals("Modify Player", modify.descriptor().name());
        assertEquals(
                List.of("target", "online", "score"), ids(modify.descriptor().inputs()));
        assertTrue(modify.descriptor().input("score").orElseThrow().optional());
        assertFalse(modify.descriptor().input("target").orElseThrow().optional());
    }

    @Test
    void documentationFillsDescriptions() {
        Documentation docs = new Documentation() {
            @Override
            public Optional<String> of(Class<?> type) {
                return type == ToyPlayer.class ? Optional.of("A player of the toy game.") : Optional.empty();
            }

            @Override
            public Optional<String> of(Method method) {
                return switch (method.getName()) {
                    case "getScore" -> Optional.of("Points scored so far.");
                    case "setScore" -> Optional.of("Replaces the score.");
                    default -> Optional.empty();
                };
            }
        };
        ReflectionRules documented =
                ReflectionRules.builder().documentation(docs).build();

        List<NodeDefinition> nodes = ReflectedType.of(ToyPlayer.class, "player").build(documented);
        EntryDefinition entry = ReflectedEntry.of(ToyHost.class, "onEvent", "event.toy")
                .parameterNames("event")
                .build(documented);

        assertEquals(
                "Points scored so far.",
                nodes.getFirst().descriptor().output("score").orElseThrow().description());
        assertEquals(
                "A player of the toy game.",
                nodes.getFirst().descriptor().input("target").orElseThrow().description());
        assertEquals(
                "Replaces the score.",
                node(nodes, "player.set_score").descriptor().description());
        assertEquals("", entry.descriptor().output("points").orElseThrow().description());
    }

    @Test
    void entryOutputsAreParameterAndGetters() {
        EntryDefinition derived =
                ReflectedEntry.of(ToyHost.class, "onEvent", "event.toy").build(RULES);
        EntryDefinition named = ReflectedEntry.of(ToyHost.class, "onEvent", "event.toy")
                .parameterNames("event")
                .exclude("cancelled")
                .build(RULES);

        assertEquals(
                List.of("toy_event", "cancelled", "player", "points"),
                ids(derived.descriptor().outputs()));
        assertEquals(
                List.of("event", "player", "points"), ids(named.descriptor().outputs()));
        assertEquals("event", named.parameters().getFirst().name());
        assertEquals(NodeKind.ENTRY, named.descriptor().kind());
        assertEquals("Toy", named.descriptor().name());
    }

    @Test
    void ambiguousOrMissingHostMethodsAreRejected() {
        assertThrows(
                IllegalArgumentException.class, () -> ReflectedEntry.of(ToyHost.class, "onMissing", "event.missing")
                        .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedEntry.of(ToyHost.class, "onEvent", "event.toy")
                .parameterNames("a", "b")
                .build(RULES));
    }

    private static GraphCompiler<ToyHost> compiler() {
        List<NodeDefinition> nodes = new ArrayList<>(ToyNodes.ALL);
        nodes.addAll(ReflectedType.of(ToyPlayer.class, "player")
                .constructor("create", Param.of("name", String.class), Param.of("score", int.class))
                .factory("fallback", "getDefault")
                .action("befriend", "befriend", Param.of("friend", String.class))
                .action("reset", "reset")
                .query("score_times", "scoreTimes", Param.of("factor", int.class))
                .build(RULES));
        nodes.addAll(ReflectedType.of(ToyPosition.class, "position").build(RULES));
        nodes.addAll(ReflectedType.of(ToyEvent.class, "event").getter(false).build(RULES));
        nodes.add(ReflectedEntry.of(ToyHost.class, "onEvent", "entry.event")
                .parameterNames("event")
                .build(RULES));
        GraphPlatform platform = GraphPlatform.builder(ToyHost.class)
                .context(ToyContext.class)
                .define(nodes)
                .classpath(List.of(codeSource()))
                .build();
        return new GraphCompiler<>(platform, ToyHost.class, ReflectionTest.class.getClassLoader());
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

    @Test
    void reflectedNodesCompileAndRun() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node(
                        "on_event",
                        NodeInstance.of("entry.event")
                                .chain("next", "set_points", "print_name", "print_points", "cancel", "bump"))
                .node("forty_two", NodeInstance.of("value.integer").config("value", IntNode.valueOf(42)))
                .node(
                        "set_points",
                        NodeInstance.of("event.set_points")
                                .input("target", "on_event", "event")
                                .input("value", "forty_two", "value"))
                .node("player", NodeInstance.of("player.get").input("target", "on_event", "player"))
                .node("print_name", NodeInstance.of("debug.print").input("value", "player", "name"))
                .node("print_points", NodeInstance.of("debug.print").input("value", "on_event", "points"))
                .node("online", NodeInstance.of("player.get").input("target", "on_event", "player"))
                .node(
                        "cancel",
                        NodeInstance.of("event.set_cancelled")
                                .input("target", "on_event", "event")
                                .input("value", "online", "online"))
                .node(
                        "bump",
                        NodeInstance.of("player.set_score")
                                .input("target", "on_event", "player")
                                .input("value", "on_event", "points"))
                .build();
        ToyContext context = new ToyContext();
        ToyPlayer player = new ToyPlayer("Lilly", 3);
        ToyEvent event = new ToyEvent(player, 7);

        compiler()
                .compile(graph, ClassName.get("generated.toy", "Reflected"))
                .instantiate(context)
                .onEvent(event);

        assertEquals(42, event.getPoints());
        assertTrue(event.isCancelled());
        assertEquals(List.of("Lilly", "42"), context.printed());
        assertEquals(42, player.getScore());
    }

    @Test
    void createdObjectsCanBeUsed() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "name", "position", "fallback"))
                .node(
                        "created",
                        NodeInstance.of("player.create")
                                .literal("name", StringNode.valueOf("Lilly"))
                                .literal("score", IntNode.valueOf(3)))
                .node("parts", NodeInstance.of("player.get").input("target", "created", "result"))
                .node("name", NodeInstance.of("debug.print").input("value", "parts", "name"))
                .node(
                        "spot",
                        NodeInstance.of("position.create")
                                .literal("x", IntNode.valueOf(1))
                                .literal("y", DoubleNode.valueOf(2.5)))
                .node("position", NodeInstance.of("debug.print").input("value", "spot", "result"))
                .node("default", NodeInstance.of("player.fallback"))
                .node("default_parts", NodeInstance.of("player.get").input("target", "default", "result"))
                .node("fallback", NodeInstance.of("debug.print").input("value", "default_parts", "name"))
                .build();
        ToyContext context = new ToyContext();

        compiler()
                .compile(graph, ClassName.get("generated.toy", "Creating"))
                .instantiate(context)
                .onStart();

        assertEquals(List.of("Lilly", "ToyPosition[x=1.0, y=2.5]", "default"), context.printed());
    }

    @Test
    void actionsRunWhereTheyAreChained() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "times", "befriend", "reset", "after"))
                .node(
                        "player",
                        NodeInstance.of("player.create")
                                .literal("name", StringNode.valueOf("Lilly"))
                                .literal("score", IntNode.valueOf(3)))
                .node(
                        "tripled",
                        NodeInstance.of("player.score_times")
                                .input("target", "player", "result")
                                .literal("factor", IntNode.valueOf(3)))
                .node("times", NodeInstance.of("debug.print").input("value", "tripled", "result"))
                .node(
                        "befriend",
                        NodeInstance.of("player.befriend")
                                .input("target", "player", "result")
                                .literal("friend", StringNode.valueOf("Mia"))
                                .chain("next", "befriended"))
                .node("befriended", NodeInstance.of("debug.print").input("value", "befriend", "result"))
                .node("reset", NodeInstance.of("player.reset").input("target", "player", "result"))
                .node("parts", NodeInstance.of("player.get").input("target", "player", "result"))
                .node("after", NodeInstance.of("debug.print").input("value", "parts", "score"))
                .build();
        ToyContext context = new ToyContext();

        compiler()
                .compile(graph, ClassName.get("generated.toy", "Calling"))
                .instantiate(context)
                .onStart();

        assertEquals(List.of("9", "true", "0"), context.printed());
    }

    @Test
    void actionResultsAreOnlyVisibleAfterTheAction() {
        Graph graph = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "befriend", "print"))
                .node("player", NodeInstance.of("player.fallback"))
                .node(
                        "befriend",
                        NodeInstance.of("player.befriend")
                                .input("target", "player", "result")
                                .literal("friend", StringNode.valueOf("Mia")))
                .node("print", NodeInstance.of("debug.print").input("value", "befriend", "result"))
                .build();

        GraphCompilationException exception = assertThrows(GraphCompilationException.class, () -> compiler()
                .compile(graph, ClassName.get("generated.toy", "Misread")));

        assertTrue(
                exception.problems().stream().anyMatch(problem -> "print".equals(problem.node())),
                exception.getMessage());
    }

    @Test
    void unusableCallsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyPlayer.class, "player")
                .action("fallback", "getDefault")
                .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyPlayer.class, "player")
                .query("reset", "reset")
                .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyPlayer.class, "player")
                .action("befriend", "befriend", Param.of("friend", int.class))
                .build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedType.of(ToyPlayer.class, "player")
                .action("home", "getHome")
                .build(RULES));
    }

    @Test
    void settersOnAnEventOnlyWorkInItsChain() {
        Graph graph = Graph.builder()
                .node("on_event", NodeInstance.of("entry.event"))
                .node("start", NodeInstance.of("entry.start").chain("next", "cancel"))
                .node(
                        "yes",
                        NodeInstance.of("logic.greater")
                                .input("a", "one", "value")
                                .input("b", "zero", "value"))
                .node("one", NodeInstance.of("value.number").config("value", IntNode.valueOf(1)))
                .node("zero", NodeInstance.of("value.number").config("value", IntNode.valueOf(0)))
                .node(
                        "cancel",
                        NodeInstance.of("event.set_cancelled")
                                .input("target", "on_event", "event")
                                .input("value", "yes", "result"))
                .build();

        GraphCompilationException exception = assertThrows(GraphCompilationException.class, () -> compiler()
                .compile(graph, ClassName.get("generated.toy", "Misplaced")));

        assertTrue(
                exception.problems().stream()
                        .anyMatch(problem -> problem.severity() == GraphProblem.Severity.ERROR
                                && "cancel".equals(problem.node())
                                && "target".equals(problem.port())),
                exception.getMessage());
    }
}
