/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.rules;

import org.bukkit.Difficulty;
import org.jspecify.annotations.Nullable;

/**
 * The difficulty a world gets during a blood night.
 */
public enum DifficultyChange {
    UNCHANGED,
    PEACEFUL,
    EASY,
    NORMAL,
    HARD;

    /**
     * @return the difficulty, {@code null} to leave it as it is
     */
    public @Nullable Difficulty difficulty() {
        return this == UNCHANGED ? null : Difficulty.valueOf(name());
    }
}
