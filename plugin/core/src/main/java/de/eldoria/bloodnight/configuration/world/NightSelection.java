/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world;

import de.eldoria.bloodnight.configuration.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.world.nightselection.NightProbability;
import de.eldoria.bloodnight.configuration.world.nightselection.NightSelectionType;
import de.eldoria.bloodnight.configuration.world.nightselection.type.Curve;
import de.eldoria.bloodnight.configuration.world.nightselection.type.Interval;
import de.eldoria.bloodnight.configuration.world.nightselection.type.MoonPhase;
import de.eldoria.bloodnight.configuration.world.nightselection.type.Phase;
import de.eldoria.bloodnight.configuration.world.nightselection.type.Random;

/**
 * How a world chooses its blood nights: the active {@code type} and the settings of every type, so switching type
 * keeps the others' settings. Holds no state; which night is which comes from its number.
 */
public class NightSelection implements NightProbability {
    private NightSelectionType type = NightSelectionType.RANDOM;
    private Random random = new Random();
    private MoonPhase moonPhase = new MoonPhase();
    private Interval interval = new Interval();
    private Phase phase = new Phase();
    private Curve curve = new Curve();

    /**
     * @return the active type
     */
    public NightSelectionType type() {
        return type;
    }

    /**
     * @param night a night
     * @return how likely it is a blood night under the active type, 0 to 100
     */
    @Override
    public int probability(Night night) {
        NightProbability active =
                switch (type) {
                    case RANDOM -> random;
                    case MOON_PHASE -> moonPhase.world();
                    case REAL_MOON_PHASE -> moonPhase.real();
                    case INTERVAL -> interval;
                    case PHASE -> phase;
                    case CURVE -> curve;
                };
        return active.probability(night);
    }
}
