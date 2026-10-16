/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import java.util.Objects;

/**
 * An armour trim: the pattern smithed onto the armour and the material it is made of.
 *
 * @param pattern  the trim pattern's key, e.g. {@code minecraft:silence}
 * @param material the trim material's key, e.g. {@code minecraft:iron}
 */
public record ItemTrim(String pattern, String material) {
    public ItemTrim {
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(material, "material");
    }
}
