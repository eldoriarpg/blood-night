/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.ClassName;
import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.graph.compiler.CompiledGraph;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphCompiler;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.reflect.Documentation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Zombie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.StringNode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginNodesTest {
    private ServerMock server;
    private Zombie zombie;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        World world = server.addSimpleWorld("world");
        zombie = world.spawn(new Location(world, 0, 64, 0), Zombie.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private CompiledGraph<CompiledBehaviour> compile(Graph graph, Predicate<String> plugins)
            throws GraphCompilationException {
        return new GraphCompiler<>(
                        BloodNightPlatform.create(
                                CompileClasspath.discover(getClass().getClassLoader()), Documentation.NONE, plugins),
                        CompiledBehaviour.class,
                        getClass().getClassLoader())
                .compile(graph, ClassName.get("generated.bukkit", "Plugins"));
    }

    private static Graph payAndPrint() {
        return Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "pay", "print"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node(
                        "player",
                        NodeInstance.of("world.nearest_player")
                                .input("location", "self_get", "location")
                                .literal("radius", DoubleNode.valueOf(100)))
                .node(
                        "pay",
                        NodeInstance.of("economy.deposit")
                                .input("player", "player", "player")
                                .literal("amount", DoubleNode.valueOf(10)))
                .node(
                        "text",
                        NodeInstance.of("placeholder.parse").literal("text", StringNode.valueOf("Hi %player_name%")))
                .node("print", NodeInstance.of("debug.print").input("value", "text", "result"))
                .build();
    }

    @Test
    void nodesOfMissingPluginsWarnAndDoNothing() throws GraphCompilationException {
        PlayerMock player = server.addPlayer();
        player.teleport(zombie.getLocation());
        CompiledGraph<CompiledBehaviour> compiled = compile(payAndPrint(), plugin -> false);

        assertEquals(
                Set.of(
                        "Needs Vault, which is not installed. This node does nothing.",
                        "Needs PlaceholderAPI, which is not installed. This node does nothing."),
                compiled.warnings().stream().map(GraphProblem::message).collect(Collectors.toSet()));

        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            compiled.instantiate(new TestContext(zombie)).onTick();
        } finally {
            System.setOut(original);
        }
        assertEquals("Hi %player_name%", buffer.toString(StandardCharsets.UTF_8).strip());
    }

    @Test
    void installedPluginsGiveNoWarning() throws GraphCompilationException {
        assertEquals(List.of(), compile(payAndPrint(), plugin -> true).warnings());
    }
}
