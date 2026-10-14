/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightProbability;
import de.eldoria.bloodnight.schema.MaxItems;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom cycle of phases: night after night the next phase applies, each with its own probability. Without phases
 * no night is a blood night. A cycle has at most {@value #MAX_PHASES} phases; further entries are ignored.
 */
public class Phase implements NightProbability {
    /**
     * The longest cycle: a hundred nights, longer than any server keeps a plan.
     */
    public static final int MAX_PHASES = 100;

    @MaxItems(MAX_PHASES)
    private List<Integer> probabilities = new ArrayList<>(List.of(50, 50, 50));

    @Override
    public int probability(Night night) {
        int phases = Math.min(probabilities.size(), MAX_PHASES);
        if (phases == 0) return 0;
        Integer value = probabilities.get((int) Math.floorMod(night.number(), (long) phases));
        return value == null ? 0 : NightProbability.percent(value);
    }
}
