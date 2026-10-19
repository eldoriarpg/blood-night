/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.item;

import de.eldoria.bloodnight.items.ItemDefinition;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.items.ItemTexts;
import de.eldoria.bloodnight.items.look.ItemAttribute;
import de.eldoria.bloodnight.items.look.ItemSlot;
import de.eldoria.bloodnight.items.look.TooltipPart;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorItemsTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void registeredItemsTakeTheirTextsAndKeepTheRest() {
        ItemStack stored = ItemStack.of(Material.DIAMOND_SWORD);
        stored.setAmount(1);
        stored.editMeta(meta -> meta.setCustomModelData(7));
        ItemRegistry current = new ItemRegistry(Map.of("abc", stored), null);
        EditorItem edit = new EditorItem(
                ItemKind.REGISTERED,
                "minecraft:stick",
                "<red>Edge",
                List.of("<gray>Sharp"),
                Map.of(),
                false,
                null,
                7,
                true,
                0.5,
                null,
                null,
                List.of(new ItemAttribute("minecraft:attack_damage", 3, Operation.ADD_NUMBER, ItemSlot.MAINHAND)),
                Set.of(TooltipPart.ENCHANTMENTS, TooltipPart.ATTRIBUTES),
                64);

        ItemRegistry applied = EditorItems.apply(current, Map.of("abc", edit));

        ItemStack item = applied.item("abc");
        assertNotNull(item);
        assertEquals(Material.DIAMOND_SWORD, item.getType());
        assertEquals(7, item.getItemMeta().getCustomModelData());
        ItemDefinition read = ItemTexts.read(item);
        assertEquals("<!italic><red>Edge", read.name());
        assertEquals(List.of("<!italic><gray>Sharp"), read.lore());
        assertEquals(false, read.glint());
        assertTrue(read.unbreakable());
        assertEquals(0.5, read.wear(), 0.01);
        assertEquals(edit.attributes(), read.attributes());
        assertEquals(Set.of(TooltipPart.ENCHANTMENTS, TooltipPart.ATTRIBUTES), read.hidden());
    }

    @Test
    void itemsMadeInTheEditorBecomeDefinitions() {
        ItemDefinition axe = new ItemDefinition(
                "minecraft:golden_axe",
                "<gold>Axe",
                List.of(),
                Map.of(),
                null,
                "myserver:blood_axe",
                null,
                false,
                null,
                null,
                null,
                List.of(),
                Set.of());
        EditorItem sword = EditorItem.defined(axe);
        ItemRegistry applied = EditorItems.apply(new ItemRegistry(), Map.of("knight_axe", sword));

        assertEquals(Map.of("knight_axe", axe), applied.definitions());
        assertEquals(Material.GOLDEN_AXE, applied.item("knight_axe").getType());
        assertEquals(
                Material.DIAMOND_HELMET,
                applied.item("minecraft:diamond_helmet").getType());
        assertNull(applied.item("unknown"));
    }

    @Test
    void oldRegistrationsAreAdoptedOnlyWhereWantedAndFree() {
        ItemRegistry current = new ItemRegistry(Map.of("kept", ItemStack.of(Material.STICK)), null);
        ItemRegistry old = new ItemRegistry(
                Map.of(
                        "kept", ItemStack.of(Material.DIAMOND),
                        "wanted", ItemStack.of(Material.BONE),
                        "dropped", ItemStack.of(Material.APPLE)),
                null);

        ItemRegistry adopted = EditorItems.adopt(current, old, Set.of("kept", "wanted"));

        assertEquals(Material.STICK, adopted.item("kept").getType());
        assertEquals(Material.BONE, adopted.item("wanted").getType());
        assertNull(adopted.registrations().get("dropped"));
    }
}
