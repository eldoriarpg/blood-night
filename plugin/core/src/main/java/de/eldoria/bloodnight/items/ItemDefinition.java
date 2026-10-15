/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An item made in the editor: a material with a name, lore and enchantments, built into an item when it is used.
 *
 * @param material     the material's key, e.g. {@code minecraft:diamond_sword}
 * @param name         the name, MiniMessage; none keeps the material's name
 * @param lore         the lore lines, MiniMessage
 * @param enchantments enchantment levels by the enchantment's key, e.g. {@code minecraft:sharpness}
 */
public record ItemDefinition(
        String material, @Nullable String name, List<String> lore, Map<String, Integer> enchantments) {
    public ItemDefinition {
        Objects.requireNonNull(material, "material");
        lore = List.copyOf(lore);
        enchantments = Map.copyOf(enchantments);
    }

    @JsonCreator
    static ItemDefinition read(
            @JsonProperty("material") String material,
            @JsonProperty("name") @Nullable String name,
            @JsonProperty("lore") @Nullable List<String> lore,
            @JsonProperty("enchantments") @Nullable Map<String, Integer> enchantments) {
        return new ItemDefinition(
                material,
                name,
                Objects.requireNonNullElse(lore, List.of()),
                Objects.requireNonNullElse(enchantments, Map.of()));
    }
}
