/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightProbability;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom cycle of phases: night after night the next phase applies, each with its own probability. Without phases
 * no night is a blood night.
 */
public class Phase implements NightProbability {
    private List<Integer> probabilities = new ArrayList<>(List.of(50, 50, 50));

    @Override
    public int probability(Night night) {
        if (probabilities.isEmpty()) return 0;
        Integer value = probabilities.get((int) Math.floorMod(night.number(), (long) probabilities.size()));
        return value == null ? 0 : NightProbability.percent(value);
    }
}
