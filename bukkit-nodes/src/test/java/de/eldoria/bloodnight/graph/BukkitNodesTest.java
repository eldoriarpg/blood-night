package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.ClassName;
import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphCompiler;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Zombie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import tools.jackson.databind.node.DoubleNode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BukkitNodesTest {
    private World world;
    private Zombie zombie;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        zombie = world.spawn(new Location(world, 1, 64, 3), Zombie.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static NodeInstance.Builder number(double value) {
        return NodeInstance.of("value.number").config("value", DoubleNode.valueOf(value));
    }

    private static NodeInstance.Builder print(String node, String port) {
        return NodeInstance.of("debug.print").input("value", node, port);
    }

    private List<String> tick(Graph graph) throws GraphCompilationException {
        CompiledBehaviour behaviour = new GraphCompiler<>(
                BloodNightPlatform.create(CompileClasspath.discover(getClass().getClassLoader())),
                CompiledBehaviour.class, getClass().getClassLoader())
                .compile(graph, ClassName.get("generated.bukkit", "Test"))
                .instantiate(new TestContext(zombie));
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            behaviour.onTick();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void locationsAreBuiltAndTakenApart() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "x", "world"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.get").input("target", "self", "entity"))
                .node("location", NodeInstance.of("location.create").input("world", "self_get", "world")
                        .input("x", "two", "value").input("y", "two", "value").input("z", "two", "value"))
                .node("two", number(2.5))
                .node("parts", NodeInstance.of("location.get").input("target", "location", "result"))
                .node("x", print("parts", "x"))
                .node("world", print("parts", "world"))
                .build();

        List<String> printed = tick(graph);

        assertEquals("2.5", printed.get(0));
        assertEquals(world.toString(), printed.get(1));
    }

    @Test
    void vectorMath() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "distance", "squared", "normalized", "direction", "unchanged"))
                .node("a", NodeInstance.of("vector.create").literal("x", DoubleNode.valueOf(0)).literal("y", DoubleNode.valueOf(0)).literal("z", DoubleNode.valueOf(0)))
                .node("b", NodeInstance.of("vector.create").literal("x", DoubleNode.valueOf(3)).literal("y", DoubleNode.valueOf(4)).literal("z", DoubleNode.valueOf(0)))
                .node("dist", NodeInstance.of("vector.distance").input("first", "a", "result").input("second", "b", "result"))
                .node("sq", NodeInstance.of("vector.distance_squared").input("first", "a", "result").input("second", "b", "result"))
                .node("norm", NodeInstance.of("vector.normalize").input("value", "b", "result"))
                .node("dir", NodeInstance.of("vector.direction").input("from", "b", "result").input("to", "a", "result"))
                .node("distance", print("dist", "result"))
                .node("squared", print("sq", "result"))
                .node("normalized", print("norm", "result"))
                .node("direction", print("dir", "result"))
                .node("unchanged", print("b", "result"))
                .build();

        assertEquals(List.of("5.0", "25.0", "0.6,0.8,0.0", "-3.0,-4.0,0.0", "3.0,4.0,0.0"), tick(graph));
    }

    @Test
    void nearbyEntitiesSeeTheMob() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "loop"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.get").input("target", "self", "entity"))
                .node("nearby", NodeInstance.of("world.nearby_entities").input("location", "self_get", "location")
                        .literal("x", DoubleNode.valueOf(5)).literal("y", DoubleNode.valueOf(5)).literal("z", DoubleNode.valueOf(5)))
                .node("loop", NodeInstance.of("flow.for_each").input("list", "nearby", "result").chain("body", "print"))
                .node("element", NodeInstance.of("entity.get").input("target", "loop", "element"))
                .node("print", print("element", "type"))
                .build();

        assertEquals(List.of("ZOMBIE"), tick(graph));
    }
}
