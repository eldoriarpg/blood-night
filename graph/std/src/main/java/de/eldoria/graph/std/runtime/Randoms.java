/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Random values as generated code writes them, statically imported: {@code nextDouble(-2.0d, 2.0d)}.
 */
public final class Randoms {
    private Randoms() {}

    /**
     * @param lower inclusive
     * @param upper exclusive
     * @return a random number in the range
     */
    public static double nextDouble(double lower, double upper) {
        return ThreadLocalRandom.current().nextDouble(lower, upper);
    }

    /**
     * @param lower inclusive
     * @param upper inclusive
     * @return a random integer in the range
     */
    public static int nextInt(int lower, int upper) {
        return ThreadLocalRandom.current().nextInt(lower, upper + 1);
    }

    /**
     * @param percent probability in percent
     * @return true with that probability
     */
    public static boolean chance(double percent) {
        return ThreadLocalRandom.current().nextDouble(100) < percent;
    }
}
