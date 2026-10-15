/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightTimesTest {
    @Test
    void nightsWithinADay() {
        NightTimes times = new NightTimes(13_000, 23_000);

        assertEquals(10_000, times.length());
        assertFalse(times.isNight(12_999));
        assertTrue(times.isNight(13_000));
        assertFalse(times.isNight(23_000));
        assertEquals(0, times.upcoming(1_000));
        assertEquals(0, times.upcoming(22_999));
        assertEquals(1, times.upcoming(23_000));
        assertEquals(24_000 + 13_000 - 25_000, times.ticksUntil(25_000, 1));
        assertEquals(3 * 24_000 + 23_000, times.endOf(3));
    }

    @Test
    void nightsWrappingPastMidnight() {
        NightTimes times = new NightTimes(18_000, 2_000);

        assertEquals(8_000, times.length());
        assertTrue(times.isNight(18_500));
        assertTrue(times.isNight(24_000 + 1_000));
        assertFalse(times.isNight(24_000 + 2_000));
        assertEquals(0, times.upcoming(24_000 + 1_000));
        assertEquals(1, times.upcoming(24_000 + 2_000));
        assertEquals(24_000 + 2_000, times.endOf(0));
    }
}
