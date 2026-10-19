/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night.selection;

import de.eldoria.bloodnight.configuration.world.NightSettings;
import de.eldoria.bloodnight.configuration.world.night.TimedNights;
import org.bukkit.World;

/**
 * Night arithmetic on a world's time, see {@link #now(World)}. Time runs in periods of {@code period} ticks, a day for
 * worlds with a day cycle; a night runs from {@code begin} to {@code end} within its period and may wrap past the
 * period's end. It is numbered by the period it begins in, so night {@code n} begins at {@code n * period + begin}. A
 * night as long as the whole period counts as no night.
 *
 * @param begin  ticks into a period the night begins
 * @param end    ticks into a period the night ends
 * @param period ticks of one period
 */
public record NightTimes(long begin, long end, long period) {
    static final long DAY = 24_000;
    static final long MINUTE = 1_200;

    /**
     * Nights of a day cycle.
     */
    public NightTimes(long begin, long end) {
        this(begin, end, DAY);
    }

    /**
     * @param settings a world's night settings
     * @return the nights of a world with a day cycle
     */
    public static NightTimes of(NightSettings settings) {
        return new NightTimes(settings.begin(), settings.end());
    }

    /**
     * @param world    a world
     * @param settings its night settings
     * @return its nights: those of its day cycle, or its timed periods when it has none
     */
    public static NightTimes of(World world, NightSettings settings) {
        if (!world.isFixedTime()) return of(settings);
        TimedNights timed = settings.timed();
        return new NightTimes(0, timed.lengthMinutes() * MINUTE, timed.everyMinutes() * MINUTE);
    }

    /**
     * @return the time nights are counted on: the time of day for worlds with a day cycle, the game time, which always
     *     runs, for worlds without one
     */
    public static long now(World world) {
        return world.isFixedTime() ? world.getGameTime() : world.getFullTime();
    }

    /**
     * @return ticks from the begin of a night to its end
     */
    public long length() {
        return Math.floorMod(end - begin, period);
    }

    /**
     * @param fullTime a world's time, see {@link #now(World)}
     * @return whether it is night then
     */
    public boolean isNight(long fullTime) {
        return Math.floorMod(fullTime - begin, period) < length();
    }

    /**
     * @param fullTime a world's time, see {@link #now(World)}
     * @return the number of the night running then, or of the next one to begin
     */
    public long upcoming(long fullTime) {
        long night = Math.floorDiv(fullTime - begin, period);
        return isNight(fullTime) ? night : night + 1;
    }

    /**
     * @param night a night's number
     * @return the time the night begins at
     */
    public long beginOf(long night) {
        return night * period + begin;
    }

    /**
     * @param night a night's number
     * @return the time the night ends at
     */
    public long endOf(long night) {
        return beginOf(night) + length();
    }

    /**
     * @param fullTime a world's time, see {@link #now(World)}
     * @param night    a night's number
     * @return ticks until that night begins; 0 when it has begun
     */
    public long ticksUntil(long fullTime, long night) {
        return Math.max(0, beginOf(night) - fullTime);
    }
}
