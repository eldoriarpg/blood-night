/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRegistryTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void registeredItemsKeepTheirDataAsOneItemUnderAStableId() {
        ItemRegistry items = new ItemRegistry();
        ItemStack sword = ItemStack.of(Material.DIAMOND_SWORD, 5);
        sword.editMeta(meta -> meta.setCustomModelData(3));

        String id = items.register(sword);

        assertEquals(id, items.register(sword));
        ItemStack stored = items.item(id);
        assertEquals(1, stored.getAmount());
        assertEquals(3, stored.getItemMeta().getCustomModelData());
        assertEquals(5, sword.getAmount());
    }

    @Test
    void onlyRegistrationsCanBeRemoved() {
        ItemRegistry items = new ItemRegistry(
                null, Map.of("axe", new ItemDefinition("minecraft:golden_axe", null, List.of(), Map.of())));
        String id = items.register(ItemStack.of(Material.STONE));

        assertTrue(items.unregister(id));
        assertNull(items.registrations().get(id));
        assertFalse(items.unregister("axe"));
        assertTrue(items.knows("axe"));
    }

    @Test
    void anItemAlreadyRegisteredKeepsItsIdAndOthersNeverTakeOne() {
        ItemStack stone = ItemStack.of(Material.STONE);
        ItemStack other = ItemStack.of(Material.DIRT);
        String taken = Integer.toHexString(stone.hashCode());
        ItemRegistry items = new ItemRegistry(Map.of(taken, other, "kept", ItemStack.of(Material.COBBLESTONE)), null);

        String id = items.register(stone);

        assertEquals(Integer.toHexString(stone.hashCode() + 1), id);
        assertEquals("kept", items.register(ItemStack.of(Material.COBBLESTONE, 12)));
        assertEquals(taken, items.find(ItemStack.of(Material.DIRT)));
        assertNull(items.find(ItemStack.of(Material.SAND)));
    }
}
