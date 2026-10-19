/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.nightselection.type;

import de.eldoria.bloodnight.configuration.world.nightselection.NightProbability;

import java.util.EnumMap;
import java.util.Map;

/**
 * A probability per phase of the moon: the world's moon, or the real moon on the date the night begins. A phase
 * missing from the map has probability 0.
 */
public class MoonPhase {
    private Map<MoonPhaseType, Integer> probabilities = new EnumMap<>(Map.of(
            MoonPhaseType.FULL_MOON, 0,
            MoonPhaseType.WANING_GIBBOUS, 10,
            MoonPhaseType.LAST_QUARTER, 20,
            MoonPhaseType.WANING_CRESCENT, 40,
            MoonPhaseType.NEW_MOON, 100,
            MoonPhaseType.WAXING_CRESCENT, 40,
            MoonPhaseType.FIRST_QUARTER, 20,
            MoonPhaseType.WAXING_GIBBOUS, 10));

    /**
     * @return probabilities by the world's moon
     */
    public NightProbability world() {
        return night -> of(MoonPhaseType.ofNight(night.number()));
    }

    /**
     * @return probabilities by the real moon
     */
    public NightProbability real() {
        return night -> of(MoonPhaseType.ofInstant(night.expectedStart()));
    }

    private int of(MoonPhaseType phase) {
        Integer value = probabilities.get(phase);
        return value == null ? 0 : NightProbability.percent(value);
    }
}
