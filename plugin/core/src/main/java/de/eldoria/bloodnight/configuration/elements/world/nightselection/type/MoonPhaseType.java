/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import java.time.Duration;
import java.time.Instant;

/**
 * The eight phases of the moon, in the order Minecraft cycles through them, starting with the full moon.
 */
public enum MoonPhaseType {
    FULL_MOON,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT,
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS;

    private static final Instant KNOWN_NEW_MOON = Instant.parse("2000-01-06T18:14:00Z");
    private static final double SYNODIC_MONTH_SECONDS = 29.530588853 * 24 * 60 * 60;

    /**
     * @param night a night's number
     * @return the phase of a world's moon in that night
     */
    public static MoonPhaseType ofNight(long night) {
        return values()[(int) Math.floorMod(night, 8L)];
    }

    /**
     * The phase of the real moon, from the mean length of a lunar month since a known new moon; accurate to about a
     * day, which is all eight phases can resolve.
     *
     * @param instant a point in time
     * @return the real moon's phase then
     */
    public static MoonPhaseType ofInstant(Instant instant) {
        double age = Duration.between(KNOWN_NEW_MOON, instant).toSeconds() / SYNODIC_MONTH_SECONDS;
        double fraction = age - Math.floor(age);
        int sinceNewMoon = (int) Math.floor(fraction * 8 + 0.5) % 8;
        return values()[(sinceNewMoon + NEW_MOON.ordinal()) % 8];
    }
}
