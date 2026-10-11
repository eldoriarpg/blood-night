/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightProbability;

/**
 * The same probability every night.
 */
public class Random implements NightProbability {
    private int probability = 60;

    @Override
    public int probability(Night night) {
        return NightProbability.percent(probability);
    }
}
