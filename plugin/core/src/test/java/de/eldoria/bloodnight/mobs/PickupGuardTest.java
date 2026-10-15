/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.loot.OrdinaryLoot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickupGuardTest {
    private WorldMock world;
    private boolean bloodNight;
    private PickupGuard guard;

    @BeforeEach
    void setUp() {
        world = MockBukkit.mock().addSimpleWorld("world");
        guard = new PickupGuard(ignored -> bloodNight, ignored -> OrdinaryLoot.defaults());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private boolean picksUp(Class<? extends LivingEntity> type) {
        Location location = new Location(world, 0, 64, 0);
        Item item = world.dropItem(location, new ItemStack(Material.DIAMOND_BLOCK));
        EntityPickupItemEvent event = new EntityPickupItemEvent(world.spawn(location, type), item, 0);
        guard.onPickup(event);
        return !event.isCancelled();
    }

    @Test
    void enemiesPickNothingUpDuringBloodNights() {
        assertTrue(picksUp(Zombie.class));
        bloodNight = true;
        assertFalse(picksUp(Zombie.class));
        assertTrue(picksUp(Villager.class));
    }
}
