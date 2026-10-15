/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightProbability;

/**
 * Every {@code interval}-th night is a blood night with {@code probability}; the others never are. An interval below 1
 * counts as 1.
 */
public class Interval implements NightProbability {
    private int interval = 5;
    private int probability = 100;

    @Override
    public int probability(Night night) {
        return Math.floorMod(night.number(), Math.max(interval, 1)) == 0 ? NightProbability.percent(probability) : 0;
    }
}
