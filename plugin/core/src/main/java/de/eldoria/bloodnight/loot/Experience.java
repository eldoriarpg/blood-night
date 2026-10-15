/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import de.eldoria.bloodnight.mob.meta.ValueModifier;

import java.util.Objects;

/**
 * How the experience a killed mob drops changes.
 *
 * @param modifier how it changes
 * @param value    the number it changes by
 */
public record Experience(ValueModifier modifier, double value) {
    /** The experience is left as the game drops it. */
    public static final Experience UNCHANGED = new Experience(ValueModifier.DEFAULT, 1);

    public Experience {
        modifier = Objects.requireNonNullElse(modifier, ValueModifier.DEFAULT);
    }

    /**
     * @param game the experience the game would drop
     * @return the experience to drop
     */
    public int apply(int game) {
        return (int) Math.round(modifier.apply(game, value));
    }
}
