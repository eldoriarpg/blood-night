/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime.value;

/**
 * The unit a cooldown length or an elapsed time is given in; a tick is 50 milliseconds.
 */
public enum TimeUnit {
    MILLISECONDS(1),
    TICKS(50),
    SECONDS(1000),
    MINUTES(60_000);

    private final long millis;

    TimeUnit(long millis) {
        this.millis = millis;
    }

    /**
     * @param amount an amount of this unit, fractions allowed
     * @return the amount in milliseconds, rounded down
     */
    public long toMillis(double amount) {
        return (long) (amount * millis);
    }

    /**
     * @param millis a time in milliseconds
     * @return the time in this unit, with fractions
     */
    public double fromMillis(long millis) {
        return (double) millis / this.millis;
    }
}
