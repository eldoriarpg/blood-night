/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import org.bukkit.World;

/**
 * What a blood night is called, which decides its texts: a blood night in worlds with a day cycle, a Blood Tide in the
 * Nether, a Blood Eclipse in the End, and the dragon fight's own announcement. The world kinds have the texts
 * {@code bloodnight.<kind>.begin}, {@code end}, {@code joined}, {@code left} and {@code name}; the dragon fight only
 * {@code begin} and {@code end}, the rest comes from the End.
 */
enum NightKind {
    NIGHT("night"),
    TIDE("tide"),
    ECLIPSE("eclipse"),
    DRAGON("dragon");

    private final String key;

    NightKind(String key) {
        this.key = key;
    }

    /**
     * @return the kind of blood night a world has
     */
    static NightKind of(World world) {
        return switch (world.getEnvironment()) {
            case NETHER -> TIDE;
            case THE_END -> ECLIPSE;
            default -> NIGHT;
        };
    }

    /**
     * @param text one of {@code begin}, {@code end}, {@code joined}, {@code left}, {@code name}
     * @return the text's key for this kind
     */
    String key(String text) {
        return "bloodnight.%s.%s".formatted(key, text);
    }
}
