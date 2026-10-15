/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShockwaveTest {
    @Test
    void reachFallsOffToTheRange() {
        assertEquals(1, Shockwave.share(new Vector(0, 0, 0), 10));
        assertEquals(0.75, Shockwave.share(new Vector(5, 0, 0), 10));
        assertEquals(0, Shockwave.share(new Vector(10, 0, 0), 10));
        assertEquals(0, Shockwave.share(new Vector(0, 20, 0), 10));
    }

    @Test
    void pushIsLiftedAndFlat() {
        Vector sideways = Shockwave.push(new Vector(4, 0, 0), 1);
        assertEquals(1, sideways.getX(), 1e-9);
        assertEquals(0.1, sideways.getY(), 1e-9);
        Vector upwards = Shockwave.push(new Vector(0, 3, 0), 1);
        assertEquals(0.2, upwards.getY(), 1e-9);
        assertNull(Shockwave.push(new Vector(0, 0, 0), 1));
    }

    @Test
    void effectsShortenWithTheDistance() {
        assertEquals(100, Shockwave.ticks(5, 1, 0.1));
        assertEquals(50, Shockwave.ticks(5, 0.5, 0.1));
        assertEquals(2, Shockwave.ticks(5, 0, 0.1));
    }
}
