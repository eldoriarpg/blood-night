/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightSelectionType;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.type.MoonPhaseType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightProbabilityTest {
    private static final Instant NOW = Instant.parse("2026-10-11T12:00:00Z");

    private static int probability(String type, long night) {
        return Selections.of("{\"type\":\"%s\"}".formatted(type)).probability(new Night(night, NOW));
    }

    @Test
    void defaultsAreTheAlphas() {
        NightSelection selection = new NightSelection();
        assertEquals(NightSelectionType.RANDOM, selection.type());
        assertEquals(60, probability("RANDOM", 7));
    }

    @Test
    void moonPhaseFollowsTheWorldsMoon() {
        assertEquals(0, probability("MOON_PHASE", 0));
        assertEquals(10, probability("MOON_PHASE", 1));
        assertEquals(100, probability("MOON_PHASE", 4));
        assertEquals(100, probability("MOON_PHASE", 12));
        assertEquals(40, probability("MOON_PHASE", 5));
    }

    @Test
    void realMoonPhaseFollowsTheRealMoon() {
        assertEquals(MoonPhaseType.FULL_MOON, MoonPhaseType.ofInstant(Instant.parse("2024-01-25T17:54:00Z")));
        assertEquals(MoonPhaseType.NEW_MOON, MoonPhaseType.ofInstant(Instant.parse("2024-01-11T11:57:00Z")));
        assertEquals(MoonPhaseType.FIRST_QUARTER, MoonPhaseType.ofInstant(Instant.parse("2024-01-18T03:53:00Z")));
        NightSelection real = Selections.of("{\"type\":\"REAL_MOON_PHASE\"}");
        assertEquals(100, real.probability(new Night(3, Instant.parse("2024-01-11T20:00:00Z"))));
        assertEquals(0, real.probability(new Night(3, Instant.parse("2024-01-25T20:00:00Z"))));
    }

    @Test
    void intervalPicksEveryNthNight() {
        assertEquals(100, probability("INTERVAL", 0));
        assertEquals(0, probability("INTERVAL", 1));
        assertEquals(100, probability("INTERVAL", 5));
        assertEquals(0, probability("INTERVAL", 9));
    }

    @Test
    void phasesCycleAndCount() {
        NightSelection phases = Selections.of("{\"type\":\"PHASE\",\"phase\":{\"probabilities\":[0,100,30]}}");
        assertEquals(0, phases.probability(new Night(0, NOW)));
        assertEquals(100, phases.probability(new Night(1, NOW)));
        assertEquals(30, phases.probability(new Night(5, NOW)));
    }

    @Test
    void curveRisesAndFallsSmoothly() {
        assertEquals(20, probability("CURVE", 0));
        assertEquals(40, probability("CURVE", 2));
        assertEquals(80, probability("CURVE", 5));
        assertEquals(40, probability("CURVE", 8));
        assertEquals(20, probability("CURVE", 10));
        for (long night = 0; night < 5; night++) {
            assertTrue(probability("CURVE", night) <= probability("CURVE", night + 1));
        }
    }

    @Test
    void valuesOutOfRangeAreClamped() {
        NightSelection wild =
                Selections.of("{\"type\":\"INTERVAL\",\"interval\":{\"interval\":0,\"probability\":250}}");
        assertEquals(100, wild.probability(new Night(3, NOW)));
    }

    @Test
    void settingsRoundTrip() {
        NightSelection phases = Selections.of("{\"type\":\"PHASE\",\"phase\":{\"probabilities\":[0,100,30]}}");
        NightSelection again = Selections.of(Selections.write(phases));
        assertEquals(NightSelectionType.PHASE, again.type());
        assertEquals(100, again.probability(new Night(1, NOW)));
    }
}
