/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import de.eldoria.bloodnight.editor.payload.MaterialInfo;
import de.eldoria.bloodnight.editor.payload.MaterialInfos;
import de.eldoria.bloodnight.items.look.ItemTrim;
import de.eldoria.bloodnight.items.look.TooltipPart;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTextsTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static ItemDefinition helmet(String material, String color, ItemTrim trim) {
        return new ItemDefinition(
                material,
                null,
                List.of(),
                Map.of(),
                null,
                "myserver:night_helmet",
                3,
                false,
                null,
                color,
                trim,
                List.of(),
                Set.of(TooltipPart.DYE));
    }

    @Test
    void lookAndColourGoOnAndComeBack() {
        ItemDefinition look = helmet("minecraft:leather_helmet", "#a01020", null);

        ItemDefinition read = ItemTexts.read(ItemTexts.build(look));

        assertEquals("myserver:night_helmet", read.model());
        assertEquals(3, read.modelData());
        assertEquals("#a01020", read.color());
        assertEquals(Set.of(TooltipPart.DYE), read.hidden());
    }

    @Test
    void enchantedBooksStoreTheirEnchantments() {
        ItemDefinition book = new ItemDefinition(
                "minecraft:enchanted_book",
                null,
                List.of(),
                Map.of("minecraft:sharpness", 3),
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                List.of(),
                Set.of());

        ItemStack built = ItemTexts.build(book);

        assertNotNull(built);
        assertEquals(Map.of(), built.getEnchantments());
        assertEquals(3, ((EnchantmentStorageMeta) built.getItemMeta()).getStoredEnchantLevel(Enchantment.SHARPNESS));
        assertEquals(Map.of("minecraft:sharpness", 3), ItemTexts.read(built).enchantments());
    }

    @Test
    void whatDoesNotFitTheMaterialIsLeftOut() {
        ItemStack sword = ItemTexts.build(helmet("minecraft:iron_sword", "#a01020", null));

        assertNotNull(sword);
        assertNull(ItemTexts.read(sword).color());
    }

    @Test
    void wearIsAShareOfTheDurability() {
        ItemStack sword = ItemStack.of(Material.DIAMOND_SWORD);

        ItemTexts.wear(sword, 0.25);

        assertEquals(0.25, ItemTexts.read(sword).wear(), 0.01);
        ItemStack stone = ItemStack.of(Material.STONE);
        ItemTexts.wear(stone, 0.5);
        assertNull(ItemTexts.read(stone).wear());
    }

    @Test
    void materialsTellWhatFitsThem() {
        MaterialInfo leather = MaterialInfos.of(Material.LEATHER_CHESTPLATE);
        MaterialInfo sword = MaterialInfos.of(Material.DIAMOND_SWORD);

        assertNotNull(leather);
        assertTrue(leather.dyeable());
        assertNotNull(sword);
        assertFalse(sword.dyeable());
        assertTrue(sword.durability() > 0);
        assertNull(MaterialInfos.of(Material.STONE));
    }
}
