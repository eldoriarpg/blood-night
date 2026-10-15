/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.assets;

import java.util.Map;

/**
 * Every item of a Minecraft version, and the names of what else the editor lets users pick from Minecraft's registries.
 *
 * @param version the version, e.g. {@code 26.3}
 * @param items   the items by key, e.g. {@code minecraft:diamond_sword}
 * @param names   names by registry, key and language: registries are {@code enchantment}, {@code entity_type},
 *                {@code effect}, {@code biome}, {@code attribute}, {@code potion} and {@code game_rule} (by the rule's
 *                language key, e.g. {@code minecraft:doInsomnia}), e.g. {@code enchantment} to
 *                {@code minecraft:sharpness} to {@code en_us: Sharpness}
 */
public record ItemIndex(
        String version, Map<String, ItemAsset> items, Map<String, Map<String, Map<String, String>>> names) {
    public ItemIndex {
        items = Map.copyOf(items);
        names = Map.copyOf(names);
    }
}
