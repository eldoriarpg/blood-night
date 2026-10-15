/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

/**
 * Where an item of the editor comes from.
 */
public enum ItemKind {
    /**
     * Registered from the game with any item data; the editor changes its name, lore and enchantments only.
     */
    REGISTERED,
    /**
     * Made in the editor from a material, a name, lore and enchantments.
     */
    DEFINED
}
