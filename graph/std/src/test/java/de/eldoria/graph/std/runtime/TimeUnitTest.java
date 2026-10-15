/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUnitTest {
    @Test
    void convertsToMillis() {
        assertEquals(1500, TimeUnit.SECONDS.toMillis(1.5));
        assertEquals(100, TimeUnit.TICKS.toMillis(2));
        assertEquals(120_000, TimeUnit.MINUTES.toMillis(2));
        assertEquals(7, TimeUnit.MILLISECONDS.toMillis(7.9));
    }

    @Test
    void convertsFromMillis() {
        assertEquals(2.5, TimeUnit.SECONDS.fromMillis(2500));
        assertEquals(3, TimeUnit.TICKS.fromMillis(150));
    }
}
