/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.ClassName;
import de.eldoria.bloodnight.api.internal.CompiledBehaviour;
import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphCompiler;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.entity.ZombieMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.StringNode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitNodesTest {
    private ServerMock server;
    private World world;
    private Zombie zombie;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        zombie = world.spawn(new Location(world, 1, 64, 3), Zombie.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static NodeInstance.Builder number(double value) {
        return NodeInstance.of("value")
                .config("type", StringNode.valueOf("number"))
                .config("value", DoubleNode.valueOf(value));
    }

    private static NodeInstance.Builder print(String node, String port) {
        return NodeInstance.of("debug.print").input("value", node, port);
    }

    private List<String> tick(Graph graph) throws GraphCompilationException {
        CompiledBehaviour behaviour = new GraphCompiler<>(
                        BloodNightPlatform.create(
                                CompileClasspath.discover(getClass().getClassLoader())),
                        CompiledBehaviour.class,
                        getClass().getClassLoader())
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
    void commandsRunWithTheMobsPlace() throws GraphCompilationException {
        List<String> ran = new ArrayList<>();
        server.getCommandMap().register("test", new Command("record") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                ran.add(sender.getName() + ": " + String.join(" ", args));
                return true;
            }
        });
        PlayerMock player = server.addPlayer("Steve");
        player.setOp(true);
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "console", "as_player"))
                .node(
                        "console",
                        NodeInstance.of("server.run_command")
                                .literal("command", StringNode.valueOf("record {world} {x} {y} {z}")))
                .node(
                        "player",
                        NodeInstance.of("world.nearest_player")
                                .input("location", "self_get", "location")
                                .literal("radius", DoubleNode.valueOf(1000)))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node(
                        "as_player",
                        NodeInstance.of("player.run_command")
                                .input("target", "player", "player")
                                .literal("command", StringNode.valueOf("record {player}")))
                .build();

        tick(graph);

        assertEquals(List.of("CONSOLE: world 1 64 3", "Steve: Steve"), ran);
    }

    @Test
    void litTntBurnsAndBreaksNothing() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "tnt"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node(
                        "tnt",
                        NodeInstance.of("world.spawn_tnt")
                                .input("location", "self_get", "location")
                                .literal("seconds", DoubleNode.valueOf(1.5)))
                .build();

        tick(graph);

        TNTPrimed tnt = world.getEntitiesByClass(TNTPrimed.class).iterator().next();
        assertEquals(30, tnt.getFuseTicks());
        assertEquals(zombie, tnt.getSource());
        assertTrue(tnt.getPersistentDataContainer().has(BukkitRuntime.HARMLESS, PersistentDataType.BOOLEAN));
    }

    @Test
    void blocksBreakAsAWitherBreaksThem() {
        Location middle = new Location(world, 10, 70, 10);
        middle.clone().add(1, 0, 0).getBlock().setType(Material.STONE);
        middle.clone().add(-1, 0, 0).getBlock().setType(Material.BEDROCK);
        middle.clone().add(0, 0, 3).getBlock().setType(Material.STONE);

        assertEquals(1, BukkitRuntime.breakBlocks(new TestContext(zombie), middle, 1.5, false));
        assertEquals(Material.AIR, middle.clone().add(1, 0, 0).getBlock().getType());
        assertEquals(Material.BEDROCK, middle.clone().add(-1, 0, 0).getBlock().getType());
        assertEquals(Material.STONE, middle.clone().add(0, 0, 3).getBlock().getType());
    }

    @Test
    void entitiesSpawnWhereTheGraphSays() {
        Entity spawned = BukkitRuntime.spawnEntity(new Location(world, 5, 64, 5), EntityType.WITHER_SKELETON);
        assertTrue(spawned instanceof WitherSkeleton);
    }

    @Test
    void broadcastsReachTheMobsWorld() throws GraphCompilationException {
        PlayerMock here = server.addPlayer();
        here.teleport(zombie.getLocation());
        PlayerMock elsewhere = server.addPlayer();
        elsewhere.teleport(server.addSimpleWorld("other").getSpawnLocation());
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "say"))
                .node(
                        "say",
                        NodeInstance.of("server.broadcast")
                                .config("reach", StringNode.valueOf("WORLD"))
                                .literal("text", StringNode.valueOf("<red>Run")))
                .build();

        tick(graph);

        assertEquals(Component.text("Run", NamedTextColor.RED), here.nextComponentMessage());
        assertNull(elsewhere.nextComponentMessage());
    }

    @Test
    void locationsAreBuiltAndTakenApart() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "x", "world"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node(
                        "location",
                        NodeInstance.of("location.create")
                                .input("world", "self_get", "world")
                                .input("x", "two", "value")
                                .input("y", "two", "value")
                                .input("z", "two", "value"))
                .node("two", number(2.5))
                .node("parts", NodeInstance.of("location.read").input("target", "location", "result"))
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
                .node(
                        "tick",
                        NodeInstance.of("mob.tick")
                                .chain("next", "distance", "squared", "normalized", "direction", "unchanged"))
                .node(
                        "a",
                        NodeInstance.of("vector.create")
                                .literal("x", DoubleNode.valueOf(0))
                                .literal("y", DoubleNode.valueOf(0))
                                .literal("z", DoubleNode.valueOf(0)))
                .node(
                        "b",
                        NodeInstance.of("vector.create")
                                .literal("x", DoubleNode.valueOf(3))
                                .literal("y", DoubleNode.valueOf(4))
                                .literal("z", DoubleNode.valueOf(0)))
                .node(
                        "dist",
                        NodeInstance.of("vector.distance")
                                .input("first", "a", "result")
                                .input("second", "b", "result"))
                .node(
                        "sq",
                        NodeInstance.of("vector.distance_squared")
                                .input("first", "a", "result")
                                .input("second", "b", "result"))
                .node("norm", NodeInstance.of("vector.normalize").input("value", "b", "result"))
                .node(
                        "dir",
                        NodeInstance.of("vector.direction")
                                .input("from", "b", "result")
                                .input("to", "a", "result"))
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
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node(
                        "nearby",
                        NodeInstance.of("world.nearby_entities")
                                .input("location", "self_get", "location")
                                .literal("x", DoubleNode.valueOf(5))
                                .literal("y", DoubleNode.valueOf(5))
                                .literal("z", DoubleNode.valueOf(5)))
                .node(
                        "loop",
                        NodeInstance.of("flow.for_each")
                                .input("list", "nearby", "result")
                                .chain("body", "print"))
                .node("element", NodeInstance.of("entity.read").input("target", "loop", "element"))
                .node("print", print("element", "type"))
                .build();

        assertEquals(List.of("ZOMBIE"), tick(graph));
    }

    @Test
    void worldChangesAndTeleportsRunOnTheirThreads() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "time"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node(
                        "time",
                        NodeInstance.of("world.mutate")
                                .input("target", "self_get", "world")
                                .literal("full_time", LongNode.valueOf(18_000))
                                .chain("next", "away"))
                .node(
                        "target",
                        NodeInstance.of("location.create")
                                .input("world", "self_get", "world")
                                .input("x", "ten", "value")
                                .input("y", "ten", "value")
                                .input("z", "ten", "value"))
                .node("ten", number(10))
                .node(
                        "away",
                        NodeInstance.of("entity.teleport")
                                .input("target", "self", "entity")
                                .input("location", "target", "result")
                                .chain("next", "print"))
                .node("print", print("away", "result"))
                .build();

        zombie = teleportingZombie();
        assertEquals(List.of("true"), tick(graph));
        assertEquals(18_000, world.getFullTime());
        assertEquals(10, zombie.getLocation().getX());
    }

    @Test
    void wholeWorldReadsWorkWithoutRegions() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "print"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node("world", NodeInstance.of("world.read").input("target", "self_get", "world"))
                .node("print", print("world", "entity_count"))
                .build();

        assertEquals(List.of("1"), tick(graph));
    }

    @Test
    void effectsPlayAndTheirArrivalRunsOnALaterTick() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "play"))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "play",
                        NodeInstance.of("world.play_effect")
                                .config("effect", StringNode.valueOf("bloodnight:blood_spear"))
                                .input("source", "self", "entity")
                                .chain("next", "played")
                                .chain("arrived", "arrived"))
                .node("played", NodeInstance.of("debug.print").literal("value", StringNode.valueOf("played")))
                .node("arrived", NodeInstance.of("debug.print").literal("value", StringNode.valueOf("arrived")))
                .build();
        CompiledBehaviour behaviour = new GraphCompiler<>(
                        BloodNightPlatform.create(
                                CompileClasspath.discover(getClass().getClassLoader())),
                        CompiledBehaviour.class,
                        getClass().getClassLoader())
                .compile(graph, ClassName.get("generated.bukkit", "Effect"))
                .instantiate(new TestContext(zombie));
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            behaviour.onTick();
            behaviour.onTick();
        } finally {
            System.setOut(original);
        }
        List<String> lines = buffer.toString(StandardCharsets.UTF_8).lines().toList();
        assertEquals(
                List.of(
                        "effect bloodnight:blood_spear",
                        "played",
                        "arrived",
                        "effect bloodnight:blood_spear",
                        "played"),
                lines);
    }

    @Test
    void spawnParticleTakesASpeed() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "spawn"))
                .node("self", NodeInstance.of("mob.self"))
                .node("self_get", NodeInstance.of("entity.read").input("target", "self", "entity"))
                .node(
                        "spawn",
                        NodeInstance.of("world.spawn_particle")
                                .input("target", "self_get", "world")
                                .literal("particle", StringNode.valueOf("minecraft:flame"))
                                .input("location", "self_get", "location")
                                .literal("count", IntNode.valueOf(3))
                                .literal("offset_x", DoubleNode.valueOf(0))
                                .literal("offset_y", DoubleNode.valueOf(0))
                                .literal("offset_z", DoubleNode.valueOf(0))
                                .literal("speed", DoubleNode.valueOf(0.25)))
                .build();
        tick(graph);
        assertEquals(1, ((WorldMock) world).getSpawnedParticles().size());
        assertEquals(0.25, ((WorldMock) world).getSpawnedParticles().getFirst().extra());
    }

    /**
     * A zombie whose asynchronous teleport finishes at once; MockBukkit does not implement {@code teleportAsync}.
     */
    private Zombie teleportingZombie() {
        ZombieMock teleporting = new ZombieMock(server, UUID.randomUUID()) {
            @Override
            public CompletableFuture<Boolean> teleportAsync(
                    Location location, PlayerTeleportEvent.TeleportCause cause, TeleportFlag... flags) {
                return CompletableFuture.completedFuture(teleport(location, cause, flags));
            }
        };
        teleporting.setLocation(new Location(world, 1, 64, 3));
        server.registerEntity(teleporting);
        return teleporting;
    }
}
