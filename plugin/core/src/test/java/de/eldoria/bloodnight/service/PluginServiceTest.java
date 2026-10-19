/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.api.BloodNightProvider;
import de.eldoria.bloodnight.api.BloodNightService;
import de.eldoria.bloodnight.api.MobInfo;
import de.eldoria.bloodnight.api.WorldInfo;
import de.eldoria.bloodnight.configuration.world.MobSettings;
import de.eldoria.bloodnight.configuration.world.NightSelection;
import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.living.MobCoordinator;
import de.eldoria.bloodnight.living.movement.Steering;
import de.eldoria.bloodnight.living.spawn.MobOutfitter;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.graph.model.Graph;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginServiceTest {
    private WorldSettings settings = new WorldSettings();
    private static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    private ServerMock server;
    private World world;
    private BloodNights nights;
    private PluginService service;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        NightSelection every = JsonMapper.builder()
                .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build()
                .readValue(
                        "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}",
                        NightSelection.class);
        NightSelector selector = new NightSelector(ignored -> every, ignored -> TIMES, Clock.systemUTC(), () -> 20);
        nights = new BloodNights(selector, ignored -> true, server.getPluginManager(), Logger.getLogger("test"));
        CustomMob zombie = Mobs.zombie("zombie", Graph.builder().build());
        ItemRegistry items = new ItemRegistry();
        MobCoordinator coordinator = new MobCoordinator(
                new MobOutfitter(() -> items, ignored -> new MobSettings()),
                () -> items,
                id -> id.equals("zombie") ? zombie : null,
                nights::running,
                new Tasks(plugin),
                Steering.NONE,
                Logger.getLogger("test"));
        service = new PluginService(
                server,
                nights,
                selector,
                coordinator,
                id -> id.equals("zombie") ? zombie : null,
                () -> List.of(zombie),
                ignored -> settings);
        server.getServicesManager().register(BloodNightService.class, service, plugin, ServicePriority.Normal);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void otherPluginsFindTheService() {
        assertSame(service, BloodNightProvider.get());
    }

    @Test
    void runningNightsAreDescribed() {
        world.setFullTime(18_000);
        nights.check(List.of(world));

        assertTrue(service.isRunning(world));
        assertEquals(Set.of(world), service.runningWorlds());
        var running = service.running(world).orElseThrow();
        assertEquals(0, running.night());
        assertEquals(5_000, running.ticksLeft());
        assertEquals(0.5, running.shareLeft(), 1e-9);
        assertFalse(running.held());
        assertEquals(100, service.probability(world, 1));
    }

    @Test
    void mobsAreSpawnedAndRecognised() {
        LivingEntity mob = service.spawn("zombie", world.getSpawnLocation()).orElseThrow();

        assertTrue(service.isCustomMob(mob));
        assertEquals("zombie", service.customMobId(mob).orElseThrow());
        assertEquals(Set.of("zombie"), service.customMobIds());
        assertTrue(service.spawn("nothing", world.getSpawnLocation()).isEmpty());
    }

    @Test
    void mobsAreDescribed() {
        MobInfo info = service.mob("zombie").orElseThrow();
        assertEquals(EntityType.ZOMBIE, info.entityType());
        assertEquals("zombie", info.name("de"));
        assertEquals(Optional.empty(), info.store());
        assertEquals(List.of("zombie"), service.mobs().stream().map(MobInfo::id).toList());
        assertEquals(Optional.empty(), service.mob("nothing"));

        LivingEntity spawned = service.spawn("zombie", world.getSpawnLocation()).orElseThrow();
        assertEquals("zombie", service.mob(spawned).orElseThrow().id());
    }

    @Test
    void worldsTellTheirWeightAndNights() {
        settings = EditorPayload.MAPPER.readValue(
                "{\"enabled\":true,\"mobSettings\":{\"spawning\":{\"mobs\":{\"zombie\":0}}}}", WorldSettings.class);
        assertEquals(0, service.weight(world, "zombie"));
        assertEquals(0, service.weight(world, "nothing"));

        WorldInfo info = service.world(world);
        assertTrue(service.isEnabled(world));
        assertEquals(service.isEnabled(world), info.enabled());
        assertEquals(world.isFixedTime(), info.timed());
        assertEquals(settings.nightSelection().type().name(), info.selection());
        assertEquals(settings.mobSettings().healthMultiplier(), info.healthMultiplier());
    }

    @Test
    void cancelledNightsHaveNoChance() {
        world.setFullTime(1_000);
        service.cancel(world);

        assertEquals(0, service.probability(world, 0));
        assertEquals(100, service.probability(world, 1));
    }
}
