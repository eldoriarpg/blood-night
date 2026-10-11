/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.configuration.elements.world.NightSettings;

/**
 * Night arithmetic on a world's full time. A night runs from {@code begin} to {@code end}, times of day in ticks, and
 * may wrap past midnight. It is numbered by the day it begins on, so night {@code n} begins at
 * {@code n * 24000 + begin}. A night as long as the whole day counts as no night.
 *
 * @param begin time of day the night begins, 0 to 23999
 * @param end   time of day the night ends, 0 to 23999
 */
public record NightTimes(long begin, long end) {
    static final long DAY = 24_000;

    /**
     * @param settings a world's night settings
     * @return the nights those settings describe
     */
    public static NightTimes of(NightSettings settings) {
        return new NightTimes(settings.begin(), settings.end());
    }

    /**
     * @return ticks from the begin of a night to its end
     */
    public long length() {
        return Math.floorMod(end - begin, DAY);
    }

    /**
     * @param fullTime a world's full time
     * @return whether it is night then
     */
    public boolean isNight(long fullTime) {
        return Math.floorMod(fullTime - begin, DAY) < length();
    }

    /**
     * @param fullTime a world's full time
     * @return the number of the night running then, or of the next one to begin
     */
    public long upcoming(long fullTime) {
        long night = Math.floorDiv(fullTime - begin, DAY);
        return isNight(fullTime) ? night : night + 1;
    }

    /**
     * @param night a night's number
     * @return the full time the night begins at
     */
    public long beginOf(long night) {
        return night * DAY + begin;
    }

    /**
     * @param night a night's number
     * @return the full time the night ends at
     */
    public long endOf(long night) {
        return beginOf(night) + length();
    }

    /**
     * @param fullTime a world's full time
     * @param night    a night's number
     * @return ticks until that night begins; 0 when it has begun
     */
    public long ticksUntil(long fullTime, long night) {
        return Math.max(0, beginOf(night) - fullTime);
    }
}
