/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob;

import org.jspecify.annotations.Nullable;

/**
 * The particle effects a mob wears without a graph, each by its id in the library. Part of the server's tuning: a
 * store update keeps it unless asked to take the store's.
 *
 * @param ambient plays over and over while the mob lives
 * @param spawn   plays once when it spawns
 * @param death   plays once where it dies
 */
public record MobParticles(
        @Nullable String ambient,
        @Nullable String spawn,
        @Nullable String death) {
    /** No effects at all. */
    public static final MobParticles NONE = new MobParticles(null, null, null);

    public MobParticles {
        ambient = blankless(ambient);
        spawn = blankless(spawn);
        death = blankless(death);
    }

    private static @Nullable String blankless(@Nullable String id) {
        return id == null || id.isBlank() ? null : id;
    }
}
