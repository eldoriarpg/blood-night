/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * A value with the weight it is picked by.
 */
record Weighted<T>(T value, int weight) {
    /**
     * @param entries the values to pick from; weights below 1 are never picked
     * @param random  the source of chance
     * @return one value, each as likely as its share of the weights; null when no weight is above 0
     */
    static <T> @Nullable T pick(List<Weighted<T>> entries, RandomGenerator random) {
        int total =
                entries.stream().mapToInt(entry -> Math.max(0, entry.weight())).sum();
        if (total <= 0) return null;
        int roll = random.nextInt(total);
        for (Weighted<T> entry : entries) {
            if (entry.weight() <= 0) continue;
            roll -= entry.weight();
            if (roll < 0) return entry.value();
        }
        return null;
    }
}
