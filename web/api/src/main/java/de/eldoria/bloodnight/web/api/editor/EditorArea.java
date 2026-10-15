/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

import org.jspecify.annotations.Nullable;

/**
 * A part of the editor payload a participant may change. Participants see every area; those they lack are read only.
 */
public enum EditorArea {
    /** The mobs, {@code mobs/<id>}. */
    MOBS,
    /** The macros, {@code macros/<id>}. */
    MACROS,
    /** The registered items, {@code items/<id>}. */
    ITEMS,
    /** The settings of each world, {@code settings/worlds/<uid>}. */
    WORLDS,
    /** The server-wide settings, {@code settings/main}. */
    SERVER,
    /** The loot tables, {@code lootTables/<id>}. */
    LOOT;

    /**
     * @param section a lockable section, e.g. {@code mobs/speed_zombie}
     * @return the area it lies in, or {@code null} for an unknown section
     */
    public static @Nullable EditorArea of(String section) {
        if (section.startsWith("mobs/")) return MOBS;
        if (section.startsWith("macros/")) return MACROS;
        if (section.startsWith("items/")) return ITEMS;
        if (section.startsWith("settings/worlds/")) return WORLDS;
        if (section.equals("settings/main")) return SERVER;
        if (section.startsWith("lootTables/")) return LOOT;
        return null;
    }
}
