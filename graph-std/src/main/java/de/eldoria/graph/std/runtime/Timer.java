/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime;

/**
 * State of a named timer. Generated code holds one per timer name and behaviour instance, shared by every node naming
 * it, so one trigger can restart what another one reads.
 */
public final class Timer {
    private long start = System.currentTimeMillis();

    /**
     * Starts the timer again.
     */
    public void reset() {
        start = System.currentTimeMillis();
    }

    /**
     * @return seconds since the behaviour was created or the timer last reset
     */
    public double seconds() {
        return (System.currentTimeMillis() - start) / 1000.0;
    }
}
