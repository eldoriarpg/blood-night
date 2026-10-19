/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.boss;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.world.MobSettings;
import de.eldoria.bloodnight.configuration.world.NightSelection;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.living.MobCoordinator;
import de.eldoria.bloodnight.living.movement.Steering;
import de.eldoria.bloodnight.living.spawn.MobOutfitter;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.attributes.Attributes;
import de.eldoria.bloodnight.mob.attributes.MobName;
import de.eldoria.bloodnight.mob.attributes.ValueModifier;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.Selections;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WitherFightTest {
    private ServerMock server;
    private World world;
    private BloodNights nights;
    private WitherFight fight;
    private CustomMob blink;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        NightSelection every = Selections.read(
                "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}", NightSelection.class);
        nights = new BloodNights(
                new NightSelector(
                        ignored -> every, ignored -> new NightTimes(13_000, 23_000), Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
        blink = wither("blink_wither");
        Tasks tasks = new Tasks(plugin);
        ItemRegistry items = new ItemRegistry();
        MobCoordinator coordinator = new MobCoordinator(
                new MobOutfitter(() -> items, ignored -> new MobSettings()),
                () -> items,
                id -> id.equals(blink.id()) ? blink : null,
                nights::running,
                tasks,
                Steering.NONE,
                Logger.getLogger("test"));
        fight = new WitherFight(
                nights,
                ignored -> Map.of("blink_wither", 1, "missing", 5),
                id -> id.equals(blink.id()) ? blink : null,
                coordinator,
                tasks);
        server.getPluginManager().registerEvents(fight, plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static CustomMob wither(String id) {
        CustomMob plain = Mobs.zombie(id, Graph.builder().build());
        return new CustomMob(
                plain.id(),
                plain.equipment(),
                new Attributes(
                        MobName.english("Blink Wither"),
                        EntityType.WITHER,
                        true,
                        ValueModifier.DEFAULT,
                        1,
                        ValueModifier.DEFAULT,
                        1),
                plain.behaviour(),
                plain.loot(),
                plain.extension(),
                plain.spawn());
    }

    private Wither build() {
        Wither wither = world.spawn(new Location(world, 0, 64, 0), Wither.class);
        server.getPluginManager()
                .callEvent(new CreatureSpawnEvent(wither, CreatureSpawnEvent.SpawnReason.BUILD_WITHER));
        server.getScheduler().performOneTick();
        return wither;
    }

    @Test
    void aWitherBuiltDuringABloodNightBecomesABoss() {
        world.setFullTime(14_000);
        nights.check(List.of(world));
        assertTrue(MobTags.isCustomMob(build()));
    }

    @Test
    void outsideBloodNightsWithersStayVanilla() {
        world.setFullTime(1_000);
        nights.check(List.of(world));
        assertFalse(MobTags.isCustomMob(build()));
    }

    @Test
    void onlyExistingWitherMobsArePicked() {
        assertEquals(blink, fight.pick(Map.of("blink_wither", 1, "missing", 5)));
        assertNull(fight.pick(Map.of("missing", 5)));
        assertNull(fight.pick(Map.of("blink_wither", 0)));
    }
}
