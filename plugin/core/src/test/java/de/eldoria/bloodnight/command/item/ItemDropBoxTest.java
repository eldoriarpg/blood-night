/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.item;

import de.eldoria.bloodnight.items.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemDropBoxTest {
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void theSameItemIsRegisteredOnceAndKnownOnesAreTold() {
        ItemRegistry registry = new ItemRegistry();
        String stone = registry.register(ItemStack.of(Material.STONE));

        Map<String, Boolean> ids = ItemDropBox.registerAll(
                registry,
                List.of(
                        ItemStack.of(Material.DIAMOND_SWORD),
                        ItemStack.of(Material.STONE, 3),
                        ItemStack.of(Material.DIAMOND_SWORD)));

        assertEquals(2, ids.size());
        assertEquals(true, ids.get(stone));
        assertEquals(2, registry.registrations().size());
    }

    @Test
    void theItemsGoBackToThePlayer() {
        PlayerMock player = server.addPlayer();

        ItemDropBox.giveBack(player, List.of(ItemStack.of(Material.STONE, 10), ItemStack.of(Material.DIAMOND_SWORD)));

        assertTrue(player.getInventory().contains(Material.STONE, 10));
        assertTrue(player.getInventory().contains(Material.DIAMOND_SWORD));
    }
}
