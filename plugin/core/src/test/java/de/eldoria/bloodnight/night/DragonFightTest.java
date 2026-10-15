/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.elements.world.MobSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSettings;
import de.eldoria.bloodnight.configuration.elements.world.night.TimedNights;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.MobName;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.MobOutfitter;
import de.eldoria.bloodnight.mobs.movement.Steering;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.time.Clock;
import java.util.List;
import java.util.OptionalLong;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DragonFightTest {
    private ServerMock server;
    private World end;
    private BloodNights nights;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        WorldMock world = new WorldMock();
        world.setName("the_end");
        world.setEnvironment(World.Environment.THE_END);
        server.addWorld(world);
        end = world;
        NightSettings settings = Selections.read("{\"timed\":{\"chance\":0}}", NightSettings.class);
        TimedNights timed = settings.timed();
        nights = new BloodNights(
                new NightSelector(
                        ignored -> timed, ignored -> NightTimes.of(end, settings), Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
        CustomMob plain = Mobs.zombie("blood_dragon", Graph.builder().build());
        CustomMob dragon = new CustomMob(
                plain.id(),
                plain.equipment(),
                new Attributes(
                        MobName.english("Blood Dragon"),
                        EntityType.ENDER_DRAGON,
                        true,
                        ValueModifier.VALUE,
                        400,
                        ValueModifier.DEFAULT,
                        1),
                plain.behaviour(),
                plain.loot(),
                plain.extension(),
                plain.spawn());
        Tasks tasks = new Tasks(plugin);
        ItemRegistry items = new ItemRegistry();
        MobCoordinator coordinator = new MobCoordinator(
                new MobOutfitter(() -> items, ignored -> new MobSettings()),
                () -> items,
                id -> id.equals(dragon.id()) ? dragon : null,
                nights::running,
                tasks,
                Steering.NONE,
                Logger.getLogger("test"));
        server.getPluginManager()
                .registerEvents(
                        new DragonFight(
                                nights,
                                ignored -> timed,
                                id -> id.equals(dragon.id()) ? dragon : null,
                                coordinator,
                                tasks),
                        plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void aDragonFightIsABloodNightWithABloodDragon() {
        EnderDragon dragon = end.spawn(new Location(end, 0, 80, 0), EnderDragon.class);
        server.getScheduler().performOneTick();
        nights.check(List.of(end));

        assertTrue(nights.running(end).isPresent());
        assertTrue(MobTags.isCustomMob(dragon));
        assertEquals(400, dragon.getAttribute(Attribute.MAX_HEALTH).getValue());

        server.getPluginManager()
                .callEvent(new EntityDeathEvent(
                        dragon,
                        org.bukkit.damage.DamageSource.builder(org.bukkit.damage.DamageType.GENERIC)
                                .build(),
                        List.of()));
        server.getScheduler().performOneTick();

        assertEquals(OptionalLong.empty(), nights.running(end));
    }
}
