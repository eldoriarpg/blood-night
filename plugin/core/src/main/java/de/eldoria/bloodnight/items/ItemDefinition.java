/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An item made in the editor: a material with a name, lore, enchantments and how it looks, wears and what it gives,
 * built into an item when it is used.
 *
 * @param material     the material's key, e.g. {@code minecraft:diamond_sword}
 * @param name         the name, MiniMessage; none keeps the material's name
 * @param lore         the lore lines, MiniMessage
 * @param enchantments enchantment levels by the enchantment's key, e.g. {@code minecraft:sharpness}
 * @param glint        whether it glints: always, never, or as the game decides by its enchantments when none
 * @param model        the key of an item model from a resource pack, e.g. {@code myserver:blood_blade}; none keeps the
 *                     material's
 * @param modelData    custom model data, for resource packs that pick a model by number; none for no number
 * @param unbreakable  whether it never wears out
 * @param wear         how worn it is, a share of its durability from 0 (new) to 1; none for new
 * @param color        the dye colour of leather armour and other dyeable items, {@code #rrggbb}; none for undyed
 * @param trim         the armour trim, for armour that takes one; none for no trim
 * @param attributes   the attribute modifiers it gives; empty keeps the material's own
 * @param hidden       the parts its tooltip leaves out
 */
public record ItemDefinition(
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
        Set<TooltipPart> hidden) {
    public ItemDefinition {
        Objects.requireNonNull(material, "material");
        lore = List.copyOf(lore);
        enchantments = Map.copyOf(enchantments);
        attributes = List.copyOf(attributes);
        hidden = hidden.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(hidden));
    }

    /**
     * @param material the material's key
     * @return a plain item of the material, as the game makes it
     */
    public static ItemDefinition of(String material) {
        return new ItemDefinition(
                material, null, List.of(), Map.of(), null, null, null, false, null, null, null, List.of(), Set.of());
    }

    @JsonCreator
    static ItemDefinition read(
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
            @JsonProperty("hidden") @Nullable Set<TooltipPart> hidden) {
        return new ItemDefinition(
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
                Objects.requireNonNullElse(hidden, Set.of()));
    }
}
