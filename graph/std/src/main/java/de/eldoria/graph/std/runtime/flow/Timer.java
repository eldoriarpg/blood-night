/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime.flow;

import de.eldoria.graph.std.runtime.value.TimeUnit;

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
     * @param unit the unit to give the time in
     * @return time since the behaviour was created or the timer last reset, with fractions
     */
    public double elapsed(TimeUnit unit) {
        return unit.fromMillis(System.currentTimeMillis() - start);
    }
}
