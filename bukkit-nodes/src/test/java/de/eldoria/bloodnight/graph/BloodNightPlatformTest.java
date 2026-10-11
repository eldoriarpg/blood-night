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
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.Port;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
class BloodNightPlatformTest {
    private ServerMock server;
    private Plugin plugin;
    private World world;
    private final Map<Integer, CompiledBehaviour> behaviours = new HashMap<>();
    private final List<RuntimeException> errors = new ArrayList<>();
    private TriggerDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        dispatcher = new TriggerDispatcher(
                entity -> Optional.ofNullable(behaviours.get(entity.getEntityId())), (trigger, e) -> errors.add(e));
        dispatcher.register(plugin, Triggers.EVENTS.stream().map(Trigger::id).toList());
    }

    @AfterEach
    void tearDown() {
        dispatcher.unregister();
        MockBukkit.unmock();
    }

    private static GraphPlatform platform() {
        return BloodNightPlatform.create(CompileClasspath.discover(BloodNightPlatformTest.class.getClassLoader()));
    }

    private static CompiledGraph<CompiledBehaviour> compile(Graph graph) throws GraphCompilationException {
        return new GraphCompiler<>(platform(), CompiledBehaviour.class, BloodNightPlatformTest.class.getClassLoader())
                .compile(graph, ClassName.get("generated.bloodnight", "Test"));
    }

    private Zombie zombie() {
        return world.spawn(new Location(world, 0, 64, 0), Zombie.class);
    }

    @Test
    void everyTriggerHasAnEntry() {
        GraphPlatform platform = platform();

        for (Trigger<?> trigger : Triggers.EVENTS) {
            assertTrue(platform.node(trigger.id()).isPresent(), trigger.id());
        }
        assertTrue(platform.node("mob.tick").isPresent());
        NodeDefinition damage = platform.node("event.damage_by_player").orElseThrow();
        List<String> outputs =
                damage.descriptor().outputs().stream().map(Port::id).toList();
        assertTrue(
                outputs.containsAll(List.of("event", "damage", "final_damage", "cause", "entity", "damager")),
                outputs.toString());
    }

    @Test
    void reflectedPaletteStaysWithinMappedTypes() {
        GraphPlatform platform = platform();

        NodeDefinition location = platform.node("location.get").orElseThrow();
        List<String> outputs =
                location.descriptor().outputs().stream().map(Port::id).toList();
        assertTrue(
                outputs.containsAll(List.of("x", "y", "z", "world", "direction", "yaw", "pitch")), outputs.toString());
        assertTrue(platform.node("cancellable.set_cancelled").isPresent());
        assertTrue(platform.node("event_data.entity_damage.set_damage").isPresent());
        assertTrue(platform.node("event_data.entity_damage.set_cancelled").isEmpty());
        platform.node("living_entity.get")
                .orElseThrow()
                .descriptor()
                .outputs()
                .forEach(port -> assertTrue(BukkitRules.accepted(port.type()), port.id() + ": " + port.type()));
    }

    @Test
    void damageTriggersKeepTheOldSplit() {
        List<String> calls = new ArrayList<>();
        Zombie zombie = zombie();
        Skeleton skeleton = world.spawn(new Location(world, 1, 64, 0), Skeleton.class);
        PlayerMock player = server.addPlayer();
        behaviours.put(zombie.getEntityId(), recorder("zombie", calls));
        behaviours.put(skeleton.getEntityId(), recorder("skeleton", calls));

        server.getPluginManager()
                .callEvent(new EntityDamageEvent(
                        zombie,
                        EntityDamageEvent.DamageCause.FALL,
                        DamageSource.builder(DamageType.GENERIC).build(),
                        1));
        server.getPluginManager()
                .callEvent(new EntityDamageByEntityEvent(
                        skeleton,
                        zombie,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC)
                                .withCausingEntity(skeleton)
                                .withDirectEntity(skeleton)
                                .build(),
                        1));
        server.getPluginManager()
                .callEvent(new EntityDamageByEntityEvent(
                        player,
                        zombie,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC)
                                .withCausingEntity(player)
                                .withDirectEntity(player)
                                .build(),
                        1));
        server.getPluginManager()
                .callEvent(new EntityDamageByEntityEvent(
                        zombie,
                        skeleton,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC)
                                .withCausingEntity(zombie)
                                .withDirectEntity(zombie)
                                .build(),
                        1000));

        assertTrue(
                calls.containsAll(List.of(
                        "zombie:damage",
                        "skeleton:hit",
                        "zombie:damage_by_entity",
                        "zombie:damage_by_player",
                        "zombie:hit",
                        "zombie:kill",
                        "zombie:entity_kill",
                        "skeleton:damage_by_entity")),
                calls.toString());
        assertEquals(8, calls.size(), calls.toString());
        assertTrue(errors.isEmpty());
    }

    private static CompiledBehaviour recorder(String name, List<String> calls) {
        return new CompiledBehaviour() {
            @Override
            public void onDamage(EntityDamageEvent event) {
                calls.add(name + ":damage");
            }

            @Override
            public void onDamageByEntity(EntityDamageByEntityEvent event) {
                calls.add(name + ":damage_by_entity");
            }

            @Override
            public void onDamageByPlayer(EntityDamageByEntityEvent event) {
                calls.add(name + ":damage_by_player");
            }

            @Override
            public void onHit(EntityDamageByEntityEvent event) {
                calls.add(name + ":hit");
            }

            @Override
            public void onKill(EntityDamageByEntityEvent event) {
                calls.add(name + ":kill");
            }

            @Override
            public void onPlayerKill(EntityDamageByEntityEvent event) {
                calls.add(name + ":player_kill");
            }

            @Override
            public void onEntityKill(EntityDamageByEntityEvent event) {
                calls.add(name + ":entity_kill");
            }
        };
    }

    @Test
    void compiledGraphChangesTheEvent() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("on_damage", NodeInstance.of("event.damage_by_player").chain("next", "set_damage"))
                .node("two", NodeInstance.of("value.number").config("value", DoubleNode.valueOf(2)))
                .node(
                        "double",
                        NodeInstance.of("math.multiply")
                                .input("first", "on_damage", "damage")
                                .input("second", "two", "value"))
                .node(
                        "set_damage",
                        NodeInstance.of("event_data.entity_damage.set_damage")
                                .input("target", "on_damage", "event")
                                .input("value", "double", "result"))
                .node("on_teleport", NodeInstance.of("event.teleport").chain("next", "cancel"))
                .node("yes", NodeInstance.of("value.boolean").config("value", BooleanNode.TRUE))
                .node(
                        "cancel",
                        NodeInstance.of("cancellable.set_cancelled")
                                .input("target", "on_teleport", "event")
                                .input("value", "yes", "value"))
                .build();
        Zombie zombie = zombie();
        behaviours.put(zombie.getEntityId(), compile(graph).instantiate(new TestContext(zombie)));
        PlayerMock player = server.addPlayer();

        EntityDamageByEntityEvent damage = new EntityDamageByEntityEvent(
                player,
                zombie,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                DamageSource.builder(DamageType.GENERIC)
                        .withCausingEntity(player)
                        .withDirectEntity(player)
                        .build(),
                3);
        server.getPluginManager().callEvent(damage);
        EntityTeleportEvent teleport = new EntityTeleportEvent(
                zombie, zombie.getLocation(), zombie.getLocation().add(5, 0, 0));
        server.getPluginManager().callEvent(teleport);

        assertEquals(6, damage.getDamage());
        assertTrue(teleport.isCancelled());
        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void registryValuesResolve() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "print"))
                .node(
                        "type",
                        NodeInstance.of("value.entity_type").config("value", StringNode.valueOf("minecraft:zombie")))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "same",
                        NodeInstance.of("logic.equal")
                                .input("first", "type", "value")
                                .input("second", "self_type", "type"))
                .node("self_type", NodeInstance.of("entity.get").input("target", "self", "entity"))
                .node("print", NodeInstance.of("debug.print").input("value", "same", "result"))
                .build();
        String source = compile(graph).source().toString();

        assertTrue(source.contains("RegistryKey.ENTITY_TYPE"), source);
        assertTrue(source.contains("\"minecraft:zombie\""), source);
        assertFalse(source.contains("EntityType.ZOMBIE"), source);
        assertTrue(platform().types().codec(PortType.of(EntityType.class)).orElseThrow() instanceof RegistryCodec<?>);
    }

    @Test
    void invalidRegistryKeysAreRejected() {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "print"))
                .node("type", NodeInstance.of("value.entity_type").config("value", StringNode.valueOf("Not A Key")))
                .node("print", NodeInstance.of("debug.print").input("value", "type", "value"))
                .build();

        GraphCompilationException exception = assertThrows(GraphCompilationException.class, () -> compile(graph));

        assertTrue(
                exception.problems().stream()
                        .anyMatch(problem -> "type".equals(problem.node()) && "value".equals(problem.port())),
                exception.getMessage());
    }
}
