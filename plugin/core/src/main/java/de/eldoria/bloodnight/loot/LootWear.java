/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import java.util.random.RandomGenerator;

/**
 * How worn an entry's item drops: a share of its durability from 0 (new) to 1, rolled anew for every drop.
 *
 * @param min the least worn it drops
 * @param max the most worn it drops
 */
public record LootWear(double min, double max) {
    public LootWear {
        min = Math.clamp(min, 0, 1);
        max = Math.clamp(Math.max(min, max), 0, 1);
    }

    /**
     * @param random the source of chance
     * @return a share from {@link #min} to {@link #max}
     */
    public double roll(RandomGenerator random) {
        return max <= min ? min : random.nextDouble(min, max);
    }
}
