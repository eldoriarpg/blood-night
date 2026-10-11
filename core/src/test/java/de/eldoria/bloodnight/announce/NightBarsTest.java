/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.night.NightTimes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NightBarsTest {
    @Test
    void barEmptiesOverTheNight() {
        NightTimes times = new NightTimes(13_000, 23_000);
        assertEquals(1f, NightBars.remaining(times, 2, 2 * 24_000 + 13_000));
        assertEquals(0.5f, NightBars.remaining(times, 2, 2 * 24_000 + 18_000));
        assertEquals(0f, NightBars.remaining(times, 2, 2 * 24_000 + 23_000));
        assertEquals(1f, NightBars.remaining(times, 2, 2 * 24_000 + 12_000));
    }

    @Test
    void barFollowsANightPastMidnight() {
        NightTimes times = new NightTimes(20_000, 4_000);
        assertEquals(0.5f, NightBars.remaining(times, 0, 24_000));
    }
}
