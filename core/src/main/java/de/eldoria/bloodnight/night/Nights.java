/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

/**
 * Night arithmetic on a world's full time. A night lasts from tick 13000 to 23000 of a day, when monsters spawn in the
 * open, and is numbered by the day it falls on, {@code fullTime / 24000}.
 */
public final class Nights {
    static final long DAY = 24_000;
    static final long BEGIN = 13_000;
    static final long END = 23_000;

    private Nights() {}

    /**
     * @param fullTime a world's full time
     * @return whether it is night then
     */
    public static boolean isNight(long fullTime) {
        long ofDay = Math.floorMod(fullTime, DAY);
        return ofDay >= BEGIN && ofDay < END;
    }

    /**
     * @param fullTime a world's full time
     * @return the number of the night running then, or of the next one to begin
     */
    public static long upcoming(long fullTime) {
        long day = Math.floorDiv(fullTime, DAY);
        return Math.floorMod(fullTime, DAY) < END ? day : day + 1;
    }

    /**
     * @param fullTime a world's full time
     * @param night    a night's number
     * @return ticks until that night begins; 0 when it has begun
     */
    public static long ticksUntil(long fullTime, long night) {
        return Math.max(0, night * DAY + BEGIN - fullTime);
    }
}
