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
import tools.jackson.databind.node.IntNode;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionTest {
    private static final ReflectionRules RULES = ReflectionRules.builder().excludeMethods("getHandlers").build();

    private static List<String> ids(List<Port> ports) {
        return ports.stream().map(Port::id).toList();
    }

    private static NodeDefinition node(List<NodeDefinition> nodes, String id) {
        return nodes.stream().filter(node -> node.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void gettersFollowTheRules() {
        assertEquals(List.of("friends", "name", "nickname", "online", "score", "url_string"),
                Properties.getters(ToyPlayer.class, RULES).stream().map(Properties.Getter::id).toList());
        assertTrue(Properties.getters(ToyPlayer.class, ReflectionRules.builder().excludeDeprecated(false).build())
                .stream().anyMatch(getter -> getter.id().equals("level")));
    }

    @Test
    void settersDisambiguateOverloads() {
        List<Properties.Setter> setters = Properties.setters(ToyPlayer.class, RULES);

        assertEquals(List.of("set_nickname", "set_nickname_string", "set_online", "set_score"),
                setters.stream().map(Properties.Setter::id).toList());
        assertEquals(PortType.of(CharSequence.class), setters.getFirst().type());
    }

    @Test
    void recordComponentsAreGetters() {
        NodeDefinition get = ReflectedType.of(ToyPosition.class, "position").build(RULES).getFirst();

        assertEquals(List.of("x", "y"), ids(get.descriptor().outputs()));
    }

    @Test
    void fluentAccessorsAreOptIn() {
        assertTrue(ReflectedType.of(ToyFluent.class, "fluent").build(RULES).isEmpty());
    }

    @Test
    void fluentAccessorsMatchFieldsOrPairs() {
        List<NodeDefinition> nodes = ReflectedType.of(ToyFluent.class, "fluent").fluent().accessors("computed").build(RULES);

        assertEquals(List.of("computed", "label", "level", "name"), ids(nodes.getFirst().descriptor().outputs()));
        assertEquals(List.of("fluent.get", "fluent.set_label", "fluent.set_name"), nodes.stream().map(NodeDefinition::id).toList());
    }

    @Test
    void fluentAccessorsOnInterfacesNeedPairs() {
        List<NodeDefinition> nodes = ReflectedType.of(ToyFluentApi.class, "api").fluent().build(RULES);
        List<NodeDefinition> manual = ReflectedType.of(ToyFluentApi.class, "api").fluent().accessors("count").build(RULES);

        assertEquals(List.of("title"), ids(nodes.getFirst().descriptor().outputs()));
        assertEquals(List.of("count", "title"), ids(manual.getFirst().descriptor().outputs()));
        assertEquals(List.of("api.get", "api.set_title"), nodes.stream().map(NodeDefinition::id).toList());
    }

    @Test
    void typeFilterAppliesToPorts() {
        ReflectionRules noLists = ReflectionRules.builder()
                .acceptTypes(type -> !(type instanceof PortType.Concrete concrete && List.class.isAssignableFrom(concrete.raw())))
                .build();

        assertFalse(Properties.getters(ToyPlayer.class, noLists).stream().anyMatch(getter -> getter.id().equals("friends")));
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

        assertEquals(List.of("toy.player.get", "toy.player.set_online", "toy.player.set_score"), nodes.stream().map(NodeDefinition::id).toList());
        NodeDefinition get = nodes.getFirst();
        assertEquals(NodeKind.PURE, get.descriptor().kind());
        assertEquals("Get Player", get.descriptor().name());
        assertEquals("toy", get.descriptor().category());
        assertEquals(List.of("target"), ids(get.descriptor().inputs()));
        assertEquals(List.of("friends", "name", "online", "score"), ids(get.descriptor().outputs()));
        assertEquals("Points", get.descriptor().output("score").orElseThrow().name());
        assertEquals(PortType.parameterized(List.class, PortType.of(String.class)), get.descriptor().output("friends").orElseThrow().type());

        NodeDefinition setScore = node(nodes, "toy.player.set_score");
        assertEquals(NodeKind.ACTION, setScore.descriptor().kind());
        assertEquals(List.of("target", "value"), ids(setScore.descriptor().inputs()));
        assertEquals(PortType.of(int.class), setScore.descriptor().input("value").orElseThrow().type());
        assertEquals("Set Player Points", setScore.descriptor().name());
    }

    @Test
    void entryOutputsAreParameterAndGetters() {
        EntryDefinition derived = ReflectedEntry.of(ToyHost.class, "onEvent", "event.toy").build(RULES);
        EntryDefinition named = ReflectedEntry.of(ToyHost.class, "onEvent", "event.toy").parameterNames("event").exclude("cancelled").build(RULES);

        assertEquals(List.of("toy_event", "cancelled", "player", "points"), ids(derived.descriptor().outputs()));
        assertEquals(List.of("event", "player", "points"), ids(named.descriptor().outputs()));
        assertEquals("event", named.parameters().getFirst().name());
        assertEquals(NodeKind.ENTRY, named.descriptor().kind());
        assertEquals("Toy", named.descriptor().name());
    }

    @Test
    void ambiguousOrMissingHostMethodsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReflectedEntry.of(ToyHost.class, "onMissing", "event.missing").build(RULES));
        assertThrows(IllegalArgumentException.class, () -> ReflectedEntry.of(ToyHost.class, "onEvent", "event.toy").parameterNames("a", "b").build(RULES));
    }

    private static GraphCompiler<ToyHost> compiler() {
        List<NodeDefinition> nodes = new ArrayList<>(ToyNodes.ALL);
        nodes.addAll(ReflectedType.of(ToyPlayer.class, "player").build(RULES));
        nodes.addAll(ReflectedType.of(ToyEvent.class, "event").getter(false).build(RULES));
        nodes.add(ReflectedEntry.of(ToyHost.class, "onEvent", "entry.event").parameterNames("event").build(RULES));
        GraphPlatform platform = GraphPlatform.builder(ToyHost.class)
                .context(ToyContext.class)
                .define(nodes)
                .classpath(List.of(codeSource()))
                .build();
        return new GraphCompiler<>(platform, ToyHost.class, ReflectionTest.class.getClassLoader());
    }

    private static Path codeSource() {
        try {
            return Path.of(ToyHost.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void reflectedNodesCompileAndRun() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("on_event", NodeInstance.of("entry.event").chain("next", "set_points", "print_name", "print_points", "cancel", "bump"))
                .node("forty_two", NodeInstance.of("value.integer").config("value", IntNode.valueOf(42)))
                .node("set_points", NodeInstance.of("event.set_points").input("target", "on_event", "event").input("value", "forty_two", "value"))
                .node("player", NodeInstance.of("player.get").input("target", "on_event", "player"))
                .node("print_name", NodeInstance.of("debug.print").input("value", "player", "name"))
                .node("print_points", NodeInstance.of("debug.print").input("value", "on_event", "points"))
                .node("online", NodeInstance.of("player.get").input("target", "on_event", "player"))
                .node("cancel", NodeInstance.of("event.set_cancelled").input("target", "on_event", "event").input("value", "online", "online"))
                .node("bump", NodeInstance.of("player.set_score").input("target", "on_event", "player").input("value", "on_event", "points"))
                .build();
        ToyContext context = new ToyContext();
        ToyPlayer player = new ToyPlayer("Lilly", 3);
        ToyEvent event = new ToyEvent(player, 7);

        compiler().compile(graph, ClassName.get("generated.toy", "Reflected")).instantiate(context).onEvent(event);

        assertEquals(42, event.getPoints());
        assertTrue(event.isCancelled());
        assertEquals(List.of("Lilly", "42"), context.printed());
        assertEquals(42, player.getScore());
    }

    @Test
    void settersOnAnEventOnlyWorkInItsChain() {
        Graph graph = Graph.builder()
                .node("on_event", NodeInstance.of("entry.event"))
                .node("start", NodeInstance.of("entry.start").chain("next", "cancel"))
                .node("yes", NodeInstance.of("logic.greater").input("a", "one", "value").input("b", "zero", "value"))
                .node("one", NodeInstance.of("value.number").config("value", IntNode.valueOf(1)))
                .node("zero", NodeInstance.of("value.number").config("value", IntNode.valueOf(0)))
                .node("cancel", NodeInstance.of("event.set_cancelled").input("target", "on_event", "event").input("value", "yes", "result"))
                .build();

        GraphCompilationException exception = assertThrows(GraphCompilationException.class,
                () -> compiler().compile(graph, ClassName.get("generated.toy", "Misplaced")));

        assertTrue(exception.problems().stream().anyMatch(problem -> problem.severity() == GraphProblem.Severity.ERROR
                && "cancel".equals(problem.node()) && "target".equals(problem.port())), exception.getMessage());
    }
}
