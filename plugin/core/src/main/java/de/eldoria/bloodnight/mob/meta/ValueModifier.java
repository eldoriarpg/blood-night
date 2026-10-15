/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

/**
 * How a configured number changes a value the game would use, such as the experience a mob drops.
 */
public enum ValueModifier {
    /** The game's value stays. */
    DEFAULT,
    /** The game's value is multiplied by the configured number. */
    MULTIPLY,
    /** The configured number replaces the game's value. */
    VALUE;

    /**
     * @param game   the game's value
     * @param number the configured number
     * @return the value to use
     */
    public double apply(double game, double number) {
        return switch (this) {
            case DEFAULT -> game;
            case MULTIPLY -> game * number;
            case VALUE -> number;
        };
    }
}
