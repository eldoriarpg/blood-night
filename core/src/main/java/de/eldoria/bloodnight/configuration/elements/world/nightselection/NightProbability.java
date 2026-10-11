/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection;

/**
 * How likely a night is to be a blood night, under one type of night selection.
 */
public interface NightProbability {
    /**
     * @param night the night
     * @return the probability in percent, 0 to 100
     */
    int probability(Night night);

    /**
     * @param value a probability as configured
     * @return the value within 0 and 100
     */
    static int percent(int value) {
        return Math.clamp(value, 0, 100);
    }
}
