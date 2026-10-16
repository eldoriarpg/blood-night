/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootWearTest {
    @Test
    void wearStaysBetweenNewAndBrokenAndWithinItsRange() {
        LootWear wear = new LootWear(0.4, 0.9);
        RandomGenerator random = RandomGenerator.of("L64X128MixRandom");

        for (int roll = 0; roll < 100; roll++) {
            double share = wear.roll(random);
            assertTrue(share >= 0.4 && share <= 0.9);
        }
        assertEquals(new LootWear(0, 1), new LootWear(-2, 5));
        assertEquals(0.7, new LootWear(0.7, 0.2).roll(random));
    }
}
