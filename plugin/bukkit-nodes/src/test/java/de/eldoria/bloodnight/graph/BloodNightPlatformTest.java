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
import de.eldoria.graph.definition.port.Port;
import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
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
                entity -> Optional.ofNullable(behaviours.get(entity.getEntityId())),
                (trigger, entity, e) -> errors.add(e));
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

        NodeDefinition location = platform.node("location.read").orElseThrow();
        List<String> outputs =
                location.descriptor().outputs().stream().map(Port::id).toList();
        assertTrue(
                outputs.containsAll(List.of("x", "y", "z", "world", "direction", "yaw", "pitch")), outputs.toString());
        assertTrue(platform.node("cancellable.mutate")
                .orElseThrow()
                .descriptor()
                .input("cancelled")
                .isPresent());
        var damage =
                platform.node("event_data.entity_damage.mutate").orElseThrow().descriptor();
        assertTrue(damage.input("damage").isPresent());
        assertTrue(damage.input("cancelled").isEmpty());
        platform.node("living_entity.read")
                .orElseThrow()
                .descriptor()
                .outputs()
                .forEach(port -> assertTrue(BukkitRules.accepted(port.type()), port.id() + ": " + port.type()));
    }

    @Test
    void damageTriggersSplitOnTheAttacker() {
        List<String> calls = new ArrayList<>();
        Zombie zombie = zombie();
        Skeleton skeleton = world.spawn(new Location(world, 1, 64, 0), Skeleton.class);
        PlayerMock player = server.addPlayer();
        behaviours.put(zombie.getEntityId(), recorder("zombie", calls));
        behaviours.put(skeleton.getEntityId(), recorder("skeleton", calls));

        server.getPluginManager()
                .callEvent(DamageEvents.of(
                        zombie,
                        EntityDamageEvent.DamageCause.FALL,
                        DamageSource.builder(DamageType.GENERIC).build(),
                        1));
        server.getPluginManager()
                .callEvent(DamageEvents.byEntity(
                        skeleton,
                        zombie,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC)
                                .withCausingEntity(skeleton)
                                .withDirectEntity(skeleton)
                                .build(),
                        1));
        server.getPluginManager()
                .callEvent(DamageEvents.byEntity(
                        player,
                        zombie,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC)
                                .withCausingEntity(player)
                                .withDirectEntity(player)
                                .build(),
                        1));
        server.getPluginManager()
                .callEvent(DamageEvents.byEntity(
                        zombie,
                        skeleton,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC)
                                .withCausingEntity(zombie)
                                .withDirectEntity(zombie)
                                .build(),
                        1));
        Arrow playerArrow = world.spawn(player.getLocation(), Arrow.class);
        server.getPluginManager()
                .callEvent(DamageEvents.byEntity(
                        playerArrow,
                        zombie,
                        EntityDamageEvent.DamageCause.PROJECTILE,
                        DamageSource.builder(DamageType.ARROW)
                                .withCausingEntity(player)
                                .withDirectEntity(playerArrow)
                                .build(),
                        1));
        Arrow skeletonArrow = world.spawn(skeleton.getLocation(), Arrow.class);
        server.getPluginManager()
                .callEvent(DamageEvents.byEntity(
                        skeletonArrow,
                        player,
                        EntityDamageEvent.DamageCause.PROJECTILE,
                        DamageSource.builder(DamageType.ARROW)
                                .withCausingEntity(skeleton)
                                .withDirectEntity(skeletonArrow)
                                .build(),
                        1));
        server.getPluginManager()
                .callEvent(new EntityDeathEvent(
                        skeleton,
                        DamageSource.builder(DamageType.GENERIC)
                                .withCausingEntity(zombie)
                                .withDirectEntity(zombie)
                                .build(),
                        List.of()));

        assertEquals(
                List.of(
                        "zombie:damage",
                        "skeleton:hit",
                        "zombie:damage_by_entity",
                        "zombie:damage_by_player",
                        "zombie:hit",
                        "skeleton:damage_by_entity",
                        "zombie:damage_by_player",
                        "skeleton:hit",
                        "zombie:kill"),
                calls);
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
            public void onKill(EntityDeathEvent event) {
                calls.add(name + ":kill");
            }
        };
    }

    @Test
    void delayedChainsRunOnALaterTick() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "wait"))
                .node(
                        "wait",
                        NodeInstance.of("flow.retriggerable_delay")
                                .config("unit", StringNode.valueOf("MILLISECONDS"))
                                .literal("duration", IntNode.valueOf(0))
                                .chain("next", "remove"))
                .node("self", NodeInstance.of("mob.self"))
                .node("remove", NodeInstance.of("entity.remove").input("target", "self", "entity"))
                .build();
        Zombie zombie = zombie();
        CompiledBehaviour behaviour = compile(graph).instantiate(new TestContext(zombie));

        behaviour.onTick();
        assertTrue(zombie.isValid());
        behaviour.onTick();
        assertFalse(zombie.isValid());
    }

    @Test
    void switchPicksTheCaseOfAnEnumValue() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("on_damage", NodeInstance.of("event.damage").chain("next", "by_cause"))
                .node(
                        "by_cause",
                        NodeInstance.of("flow.switch")
                                .config("type", StringNode.valueOf("damage_cause"))
                                .config("case_0", StringNode.valueOf("FALL"))
                                .input("value", "on_damage", "cause")
                                .chain("case_0", "cancel"))
                .node(
                        "cancel",
                        NodeInstance.of("cancellable.mutate")
                                .input("target", "on_damage", "event")
                                .literal("cancelled", BooleanNode.TRUE))
                .build();
        Zombie zombie = zombie();
        behaviours.put(zombie.getEntityId(), compile(graph).instantiate(new TestContext(zombie)));

        EntityDamageEvent fall = DamageEvents.of(
                zombie,
                EntityDamageEvent.DamageCause.FALL,
                DamageSource.builder(DamageType.FALL).build(),
                1);
        EntityDamageEvent fire = DamageEvents.of(
                zombie,
                EntityDamageEvent.DamageCause.FIRE,
                DamageSource.builder(DamageType.IN_FIRE).build(),
                1);
        server.getPluginManager().callEvent(fall);
        server.getPluginManager().callEvent(fire);

        assertTrue(fall.isCancelled());
        assertFalse(fire.isCancelled());
        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void compiledGraphChangesTheEvent() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("on_damage", NodeInstance.of("event.damage_by_player").chain("next", "set_damage"))
                .node(
                        "two",
                        NodeInstance.of("value")
                                .config("type", StringNode.valueOf("number"))
                                .config("value", DoubleNode.valueOf(2)))
                .node(
                        "double",
                        NodeInstance.of("math.multiply")
                                .input("first", "on_damage", "damage")
                                .input("second", "two", "value"))
                .node(
                        "set_damage",
                        NodeInstance.of("event_data.entity_damage.mutate")
                                .input("target", "on_damage", "event")
                                .input("damage", "double", "result"))
                .node("on_teleport", NodeInstance.of("event.teleport").chain("next", "cancel"))
                .node(
                        "yes",
                        NodeInstance.of("value")
                                .config("type", StringNode.valueOf("boolean"))
                                .config("value", BooleanNode.TRUE))
                .node(
                        "cancel",
                        NodeInstance.of("cancellable.mutate")
                                .input("target", "on_teleport", "event")
                                .input("cancelled", "yes", "value"))
                .build();
        Zombie zombie = zombie();
        behaviours.put(zombie.getEntityId(), compile(graph).instantiate(new TestContext(zombie)));
        PlayerMock player = server.addPlayer();

        EntityDamageByEntityEvent damage = DamageEvents.byEntity(
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
    void attackerIsWhoShotTheArrow() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("hurt", NodeInstance.of("event.damage_by_player").chain("next", "branch"))
                .node(
                        "as_player",
                        NodeInstance.of("cast")
                                .config("type", StringNode.valueOf("player"))
                                .input("value", "hurt", "attacker"))
                .node(
                        "branch",
                        NodeInstance.of("flow.branch")
                                .input("condition", "as_player", "matches")
                                .chain("true", "cancel"))
                .node(
                        "cancel",
                        NodeInstance.of("cancellable.mutate")
                                .input("target", "hurt", "event")
                                .literal("cancelled", BooleanNode.TRUE))
                .build();
        Zombie zombie = zombie();
        behaviours.put(zombie.getEntityId(), compile(graph).instantiate(new TestContext(zombie)));
        PlayerMock player = server.addPlayer();
        Arrow arrow = world.spawn(player.getLocation(), Arrow.class);

        EntityDamageByEntityEvent damage = DamageEvents.byEntity(
                arrow,
                zombie,
                EntityDamageEvent.DamageCause.PROJECTILE,
                DamageSource.builder(DamageType.ARROW)
                        .withCausingEntity(player)
                        .withDirectEntity(arrow)
                        .build(),
                3);
        server.getPluginManager().callEvent(damage);

        assertTrue(damage.isCancelled());
        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void modifyAppliesOnlyTheGivenProperties() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "modify"))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "modify",
                        NodeInstance.of("entity.mutate")
                                .input("target", "self", "entity")
                                .literal("fire_ticks", IntNode.valueOf(80))
                                .literal("glowing", BooleanNode.TRUE))
                .build();
        Zombie zombie = zombie();
        zombie.setSilent(true);
        CompiledGraph<CompiledBehaviour> compiled = compile(graph);
        compiled.instantiate(new TestContext(zombie)).onTick();

        assertEquals(80, zombie.getFireTicks());
        assertTrue(zombie.isGlowing());
        assertTrue(zombie.isSilent());
        assertFalse(compiled.source().contains("setSilent"));
    }

    @Test
    void killEntriesCheckTheirVictim() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("any", NodeInstance.of("event.kill").chain("next", "print_any"))
                .node(
                        "players",
                        NodeInstance.of("event.kill")
                                .config("victim", StringNode.valueOf("PLAYER"))
                                .chain("next", "print_player"))
                .node("print_any", NodeInstance.of("debug.print").literal("value", StringNode.valueOf("any")))
                .node("print_player", NodeInstance.of("debug.print").literal("value", StringNode.valueOf("player")))
                .build();

        String source = compile(graph).source();

        assertEquals(1, source.split("instanceof Player", -1).length - 1, source);
        assertFalse(source.contains("!(event.getEntity() instanceof"), source);
    }

    @Test
    void selfIsTheMobsOwnEntityType() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "fuse"))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "fuse",
                        NodeInstance.of("creeper.mutate")
                                .input("target", "self", "entity")
                                .literal("max_fuse_ticks", IntNode.valueOf(5)))
                .build();
        GraphCompiler<CompiledBehaviour> compiler = new GraphCompiler<>(
                platform(), CompiledBehaviour.class, getClass().getClassLoader());

        String source = compiler.compile(
                        graph,
                        ClassName.get("generated.bloodnight", "Creeping"),
                        MobBindings.of(EntityType.CREEPER, EntityType.VEX))
                .source();

        assertTrue(source.contains("(Creeper)"), source);
        assertThrows(
                GraphCompilationException.class,
                () -> compiler.compile(graph, ClassName.get("generated.bloodnight", "Unbound")),
                "a graph checked without its mob keeps Self a living entity");
    }

    @Test
    void attributesCanBeDefinedAsValues() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "print"))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "speed",
                        NodeInstance.of("value")
                                .config("type", StringNode.valueOf("attribute"))
                                .config("value", StringNode.valueOf("minecraft:movement_speed")))
                .node(
                        "instance",
                        NodeInstance.of("living_entity.attribute")
                                .input("target", "self", "entity")
                                .input("attribute", "speed", "value"))
                .node("print", NodeInstance.of("debug.print").input("value", "instance", "result"))
                .build();

        String source = compile(graph).source();

        assertTrue(source.contains("getAttribute("), source);
        assertTrue(source.contains("movement_speed") || source.contains("MOVEMENT_SPEED"), source);
    }

    @Test
    void applyEffectGivesPotionEffects() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("hurt", NodeInstance.of("event.damage_by_player").chain("next", "apply"))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "apply",
                        NodeInstance.of("living_entity.apply_effect")
                                .input("target", "self", "entity")
                                .literal("effect", StringNode.valueOf("minecraft:regeneration"))
                                .literal("seconds", DoubleNode.valueOf(5))
                                .literal("amplifier", IntNode.valueOf(1)))
                .build();
        Zombie zombie = zombie();
        behaviours.put(zombie.getEntityId(), compile(graph).instantiate(new TestContext(zombie)));
        PlayerMock player = server.addPlayer();

        server.getPluginManager()
                .callEvent(DamageEvents.byEntity(
                        player,
                        zombie,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC).build(),
                        1));

        PotionEffect effect = zombie.getPotionEffect(PotionEffectType.REGENERATION);
        assertTrue(effect != null && effect.getDuration() == 100 && effect.getAmplifier() == 1, String.valueOf(effect));
        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void castsLetTheMobPoisonWhatItHits() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("hit", NodeInstance.of("event.hit").chain("next", "guard"))
                .node(
                        "victim",
                        NodeInstance.of("cast")
                                .config("type", StringNode.valueOf("living_entity"))
                                .input("value", "hit", "entity"))
                .node(
                        "guard",
                        NodeInstance.of("flow.branch")
                                .input("condition", "victim", "matches")
                                .chain("true", "poison"))
                .node(
                        "poison",
                        NodeInstance.of("living_entity.apply_effect")
                                .input("target", "victim", "result")
                                .literal("effect", StringNode.valueOf("minecraft:poison"))
                                .literal("seconds", DoubleNode.valueOf(3))
                                .literal("amplifier", IntNode.valueOf(0)))
                .build();
        Zombie zombie = zombie();
        Skeleton skeleton = world.spawn(new Location(world, 1, 64, 0), Skeleton.class);
        behaviours.put(zombie.getEntityId(), compile(graph).instantiate(new TestContext(zombie)));

        server.getPluginManager()
                .callEvent(DamageEvents.byEntity(
                        zombie,
                        skeleton,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        DamageSource.builder(DamageType.GENERIC).build(),
                        1));

        assertTrue(skeleton.hasPotionEffect(PotionEffectType.POISON));
        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void registryValuesResolve() throws GraphCompilationException {
        Graph graph = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "print"))
                .node(
                        "type",
                        NodeInstance.of("value")
                                .config("type", StringNode.valueOf("entity_type"))
                                .config("value", StringNode.valueOf("minecraft:zombie")))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "same",
                        NodeInstance.of("logic.equal")
                                .input("first", "type", "value")
                                .input("second", "self_type", "type"))
                .node("self_type", NodeInstance.of("entity.read").input("target", "self", "entity"))
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
                .node(
                        "type",
                        NodeInstance.of("value")
                                .config("type", StringNode.valueOf("entity_type"))
                                .config("value", StringNode.valueOf("Not A Key")))
                .node("print", NodeInstance.of("debug.print").input("value", "type", "value"))
                .build();

        GraphCompilationException exception = assertThrows(GraphCompilationException.class, () -> compile(graph));

        assertTrue(
                exception.problems().stream()
                        .anyMatch(problem -> "type".equals(problem.node()) && "value".equals(problem.port())),
                exception.getMessage());
    }
}
