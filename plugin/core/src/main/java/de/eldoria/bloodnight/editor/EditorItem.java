/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.items.ItemDefinition;
import de.eldoria.bloodnight.items.ItemTexts;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An item as the editor shows and edits it.
 *
 * @param kind         whether it was registered from the game or made in the editor
 * @param material     the material's key
 * @param name         the name, MiniMessage; none for the material's own name
 * @param lore         the lore lines, MiniMessage
 * @param enchantments enchantment levels by the enchantment's key
 * @param glint            whether it glints: always, never, or as the game decides by its enchantments when none
 * @param hideEnchantments whether its tooltip leaves out the enchantments
 * @param stackSize    how many fit in one stack, for an item registered from the game, which may differ from its
 *                     material's; none for an item made in the editor, which stacks like its material
 */
public record EditorItem(
        ItemKind kind,
        String material,
        @Nullable String name,
        List<String> lore,
        Map<String, Integer> enchantments,
        @Nullable Boolean glint,
        boolean hideEnchantments,
        @Nullable Integer stackSize) {
    public EditorItem {
        lore = List.copyOf(lore);
        enchantments = Map.copyOf(enchantments);
    }

    @JsonCreator
    static EditorItem read(
            @JsonProperty("kind") @Nullable ItemKind kind,
            @JsonProperty("material") String material,
            @JsonProperty("name") @Nullable String name,
            @JsonProperty("lore") @Nullable List<String> lore,
            @JsonProperty("enchantments") @Nullable Map<String, Integer> enchantments,
            @JsonProperty("glint") @Nullable Boolean glint,
            @JsonProperty("hideEnchantments") @Nullable Boolean hideEnchantments,
            @JsonProperty("stackSize") @Nullable Integer stackSize) {
        return new EditorItem(
                Objects.requireNonNullElse(kind, ItemKind.DEFINED),
                material,
                name,
                Objects.requireNonNullElse(lore, List.of()),
                Objects.requireNonNullElse(enchantments, Map.of()),
                glint,
                Boolean.TRUE.equals(hideEnchantments),
                stackSize);
    }

    /**
     * @param item an item registered from the game
     * @return how the editor shows it
     */
    public static EditorItem registered(ItemStack item) {
        return new EditorItem(
                ItemKind.REGISTERED,
                item.getType().getKey().asString(),
                ItemTexts.name(item),
                ItemTexts.lore(item),
                ItemTexts.enchantments(item),
                ItemTexts.glint(item),
                ItemTexts.hidesEnchantments(item),
                item.getMaxStackSize());
    }

    /**
     * @param definition an item made in the editor
     * @return how the editor shows it
     */
    public static EditorItem defined(ItemDefinition definition) {
        return new EditorItem(
                ItemKind.DEFINED,
                definition.material(),
                definition.name(),
                definition.lore(),
                definition.enchantments(),
                definition.glint(),
                definition.hideEnchantments(),
                null);
    }

    /**
     * @return the item as the item registry keeps an item made in the editor
     */
    public ItemDefinition definition() {
        return new ItemDefinition(material, name, lore, enchantments, glint, hideEnchantments);
    }
}
