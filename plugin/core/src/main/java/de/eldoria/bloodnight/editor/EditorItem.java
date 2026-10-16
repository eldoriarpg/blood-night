/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.items.ItemAttribute;
import de.eldoria.bloodnight.items.ItemDefinition;
import de.eldoria.bloodnight.items.ItemTexts;
import de.eldoria.bloodnight.items.ItemTrim;
import de.eldoria.bloodnight.items.TooltipPart;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An item as the editor shows and edits it. The fields past {@code kind} are those of {@link ItemDefinition}; an item
 * registered from the game shows what it carries and takes what the editor changes, keeping everything else.
 *
 * @param kind         whether it was registered from the game or made in the editor
 * @param material     the material's key
 * @param name         the name, MiniMessage; none for the material's own name
 * @param lore         the lore lines, MiniMessage
 * @param enchantments enchantment levels by the enchantment's key
 * @param glint        whether it glints: always, never, or as the game decides by its enchantments when none
 * @param model        the key of an item model from a resource pack; none for the material's
 * @param modelData    custom model data; none for no number
 * @param unbreakable  whether it never wears out
 * @param wear         how worn it is, from 0 (new) to 1; none for new
 * @param color        the dye colour, {@code #rrggbb}; none for undyed
 * @param trim         the armour trim; none for no trim
 * @param attributes   the attribute modifiers it gives; empty keeps the material's own
 * @param hidden       the parts its tooltip leaves out
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
        @Nullable String model,
        @Nullable Integer modelData,
        boolean unbreakable,
        @Nullable Double wear,
        @Nullable String color,
        @Nullable ItemTrim trim,
        List<ItemAttribute> attributes,
        Set<TooltipPart> hidden,
        @Nullable Integer stackSize) {
    public EditorItem {
        lore = List.copyOf(lore);
        enchantments = Map.copyOf(enchantments);
        attributes = List.copyOf(attributes);
        hidden = hidden.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(hidden));
    }

    @JsonCreator
    static EditorItem read(
            @JsonProperty("kind") @Nullable ItemKind kind,
            @JsonProperty("material") String material,
            @JsonProperty("name") @Nullable String name,
            @JsonProperty("lore") @Nullable List<String> lore,
            @JsonProperty("enchantments") @Nullable Map<String, Integer> enchantments,
            @JsonProperty("glint") @Nullable Boolean glint,
            @JsonProperty("model") @Nullable String model,
            @JsonProperty("modelData") @Nullable Integer modelData,
            @JsonProperty("unbreakable") @Nullable Boolean unbreakable,
            @JsonProperty("wear") @Nullable Double wear,
            @JsonProperty("color") @Nullable String color,
            @JsonProperty("trim") @Nullable ItemTrim trim,
            @JsonProperty("attributes") @Nullable List<ItemAttribute> attributes,
            @JsonProperty("hidden") @Nullable Set<TooltipPart> hidden,
            @JsonProperty("stackSize") @Nullable Integer stackSize) {
        return new EditorItem(
                Objects.requireNonNullElse(kind, ItemKind.DEFINED),
                material,
                name,
                Objects.requireNonNullElse(lore, List.of()),
                Objects.requireNonNullElse(enchantments, Map.of()),
                glint,
                model,
                modelData,
                Boolean.TRUE.equals(unbreakable),
                wear,
                color,
                trim,
                Objects.requireNonNullElse(attributes, List.of()),
                Objects.requireNonNullElse(hidden, Set.of()),
                stackSize);
    }

    /**
     * @param item an item registered from the game
     * @return how the editor shows it
     */
    public static EditorItem registered(ItemStack item) {
        ItemDefinition look = ItemTexts.read(item);
        return of(ItemKind.REGISTERED, look, item.getMaxStackSize());
    }

    /**
     * @param definition an item made in the editor
     * @return how the editor shows it
     */
    public static EditorItem defined(ItemDefinition definition) {
        return of(ItemKind.DEFINED, definition, null);
    }

    private static EditorItem of(ItemKind kind, ItemDefinition look, @Nullable Integer stackSize) {
        return new EditorItem(
                kind,
                look.material(),
                look.name(),
                look.lore(),
                look.enchantments(),
                look.glint(),
                look.model(),
                look.modelData(),
                look.unbreakable(),
                look.wear(),
                look.color(),
                look.trim(),
                look.attributes(),
                look.hidden(),
                stackSize);
    }

    /**
     * @return the item as the item registry keeps an item made in the editor
     */
    public ItemDefinition definition() {
        return new ItemDefinition(
                material,
                name,
                lore,
                enchantments,
                glint,
                model,
                modelData,
                unbreakable,
                wear,
                color,
                trim,
                attributes,
                hidden);
    }
}
