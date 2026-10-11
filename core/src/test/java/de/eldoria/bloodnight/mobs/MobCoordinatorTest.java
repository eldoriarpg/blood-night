/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.graph.TriggerDispatcher;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.IntNode;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
class MobCoordinatorTest {
    private static GraphPlatform platform;
    private final Map<String, CustomMob> definitions = new HashMap<>();
    private ServerMock server;
    private World world;
    private MobCoordinator coordinator;
    private BehaviourService service;
    private TriggerDispatcher dispatcher;

    @TempDir
    Path cacheDir;

    @BeforeAll
    static void platform() {
        platform = BloodNightPlatform.create(CompileClasspath.discover(MobCoordinatorTest.class.getClassLoader()));
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        ItemRegistry items = new ItemRegistry();
        coordinator = new MobCoordinator(
                new MobOutfitter(() -> items), () -> items, definitions::get, Logger.getLogger("test"));
        service = new BehaviourService(
                platform, getClass().getClassLoader(), new BytecodeCache(cacheDir), "test", Logger.getLogger("test"));
        dispatcher = new TriggerDispatcher(coordinator::behaviour, (trigger, error) -> {
            throw error;
        });
        dispatcher.register(plugin, List.of("event.damage"));
    }

    @AfterEach
    void tearDown() {
        dispatcher.unregister();
        service.close();
        MockBukkit.unmock();
    }

    private static Graph fireTicks(int ticks) {
        return Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "burn"))
                .node("self", NodeInstance.of("mob.self"))
                .node(
                        "burn",
                        NodeInstance.of("entity.set_fire_ticks")
                                .input("target", "self", "entity")
                                .literal("value", IntNode.valueOf(ticks)))
                .node("damage", NodeInstance.of("event.damage").chain("next", "cancel"))
                .node(
                        "cancel",
                        NodeInstance.of("cancellable.set_cancelled")
                                .input("target", "damage", "event")
                                .literal("value", BooleanNode.TRUE))
                .build();
    }

    private void define(CustomMob... mobs) {
        definitions.clear();
        for (CustomMob mob : mobs) definitions.put(mob.id(), mob);
        Behaviours behaviours = service.compileNow(definitions.values(), List.of());
        assertTrue(behaviours.problems().isEmpty(), behaviours.problems().toString());
        coordinator.rebind(behaviours);
    }

    private Zombie zombie() {
        return world.spawn(new Location(world, 0, 64, 0), Zombie.class);
    }

    @Test
    void registeredMobsRunTheirBehaviour() {
        define(Mobs.zombie("burner", fireTicks(100)));
        Zombie zombie = zombie();
        coordinator.register(zombie, definitions.get("burner"));

        coordinator.tick();
        EntityDamageEvent damage = new EntityDamageEvent(
                zombie,
                EntityDamageEvent.DamageCause.FALL,
                DamageSource.builder(DamageType.GENERIC).build(),
                3);
        server.getPluginManager().callEvent(damage);

        assertEquals(100, zombie.getFireTicks());
        assertTrue(damage.isCancelled());
        assertTrue(MobTags.isCustomMob(zombie));
    }

    @Test
    void otherEntitiesAreLeftAlone() {
        define(Mobs.zombie("burner", fireTicks(100)));
        Zombie plain = zombie();

        coordinator.tick();
        EntityDamageEvent damage = new EntityDamageEvent(
                plain,
                EntityDamageEvent.DamageCause.FALL,
                DamageSource.builder(DamageType.GENERIC).build(),
                3);
        server.getPluginManager().callEvent(damage);

        assertFalse(damage.isCancelled());
        assertTrue(coordinator.behaviour(plain).isEmpty());
    }

    @Test
    void rebindSwitchesLivingMobsToNewBehaviours() {
        define(Mobs.zombie("burner", fireTicks(100)));
        Zombie zombie = zombie();
        coordinator.register(zombie, definitions.get("burner"));
        var before = coordinator.behaviour(zombie).orElseThrow();

        define(Mobs.zombie("burner", fireTicks(250)));
        coordinator.tick();

        assertEquals(250, zombie.getFireTicks());
        assertFalse(
                before.getClass() == coordinator.behaviour(zombie).orElseThrow().getClass());
    }

    @Test
    void removedAndInvalidMobsAreForgotten() {
        define(Mobs.zombie("burner", fireTicks(100)));
        Zombie removed = zombie();
        Zombie dead = zombie();
        coordinator.register(removed, definitions.get("burner"));
        coordinator.register(dead, definitions.get("burner"));

        coordinator.remove(removed);
        dead.remove();
        coordinator.tick();

        assertTrue(coordinator.active().isEmpty());
    }

    @Test
    void taggedEntitiesAreTakenOverAgain() {
        define(Mobs.zombie("burner", fireTicks(100)));
        Zombie zombie = zombie();
        coordinator.register(zombie, definitions.get("burner"));
        coordinator.remove(zombie);

        assertTrue(coordinator.attach(zombie).isPresent());
        coordinator.tick();

        assertEquals(100, zombie.getFireTicks());
    }

    @Test
    void failingBehavioursDoNotStopOthers() {
        Graph throwing = Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "boom"))
                .node("extension", NodeInstance.of("mob.extension"))
                .node(
                        "boom",
                        NodeInstance.of("entity.set_fire_ticks")
                                .input("target", "extension", "entity")
                                .literal("value", IntNode.valueOf(1)))
                .build();
        define(Mobs.zombie("broken", throwing), Mobs.zombie("burner", fireTicks(100)));
        Zombie broken = zombie();
        Zombie burner = zombie();
        coordinator.register(broken, definitions.get("broken"));
        coordinator.register(burner, definitions.get("burner"));

        coordinator.tick();
        coordinator.tick();

        assertEquals(100, burner.getFireTicks());
    }
}
