/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.announcement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundRangeTest {
    @Test
    void picksWithinTheRangeInEitherOrder() {
        for (int i = 0; i < 200; i++) {
            float picked = new SoundRange(1.2f, 0.8f).pick(0f, 2f);
            assertTrue(picked >= 0.8f && picked <= 1.2f, String.valueOf(picked));
        }
    }

    @Test
    void anEmptyRangeAlwaysGivesItsValue() {
        assertEquals(0.7f, new SoundRange(0.7f, 0.7f).pick(0f, 2f));
    }

    @Test
    void picksStayWithinWhatTheClientPlays() {
        assertEquals(0.5f, new SoundRange(0.1f, 0.1f).pick(0.5f, 2f));
        assertEquals(1f, new SoundRange(3f, 3f).pick(0f, 1f));
    }
}
