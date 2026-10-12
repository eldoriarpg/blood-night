/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;
import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.world.WorldDrops;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DropHandlerTest {
    private ServerMock server;
    private WorldMock world;
    private DropHandler handler;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        CustomMob zombie = Mobs.zombie("gemmy", Graph.builder().build());
        CustomMob gemmy = new CustomMob(
                zombie.id(),
                zombie.equipment(),
                zombie.attributes(),
                zombie.behaviour(),
                new MobDrops(
                        ConfigurationScope.MOB,
                        Map.of(1, 1),
                        List.of(ConfigurationScope.MOB),
                        List.of(new Drop("minecraft:diamond", 3, 1)),
                        ConfigurationScope.MOB,
                        ValueModifier.VALUE,
                        10),
                zombie.extension(),
                zombie.spawn());
        ItemRegistry items = new ItemRegistry(null, null);
        handler = new DropHandler(
                id -> id.equals("gemmy") ? gemmy : null,
                world -> WorldDrops.defaultDrops(),
                GlobalDrops::defaultDrops,
                items::item,
                RandomGenerator.of("L64X128MixRandom"));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private EntityDeathEvent death(boolean byPlayer) {
        Zombie zombie = world.spawn(new Location(world, 0, 64, 0), Zombie.class);
        MobTags.markCustomMob(zombie, Mobs.zombie("gemmy", Graph.builder().build()));
        if (byPlayer) zombie.setKiller(server.addPlayer());
        EntityDeathEvent event = new EntityDeathEvent(
                zombie,
                DamageSource.builder(DamageType.GENERIC).build(),
                new ArrayList<>(List.of(new ItemStack(Material.ROTTEN_FLESH))),
                5);
        handler.onDeath(event);
        return event;
    }

    @Test
    void aPlayerKillAddsTheDropsToTheVanillaLoot() {
        EntityDeathEvent event = death(true);

        assertEquals(
                List.of(new ItemStack(Material.ROTTEN_FLESH), new ItemStack(Material.DIAMOND, 3)), event.getDrops());
        assertEquals(10, event.getDroppedExp());
    }

    @Test
    void otherDeathsKeepTheirVanillaLootOnly() {
        EntityDeathEvent event = death(false);

        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), event.getDrops());
        assertEquals(5, event.getDroppedExp());
    }
}
