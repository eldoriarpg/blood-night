/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime.flow;

/**
 * State of a cooldown node. Generated code holds one per node and behaviour instance.
 */
public final class Cooldown {
    private long last = Long.MIN_VALUE;

    /**
     * Reports whether the cooldown has passed and restarts it if so. The first call is always ready.
     *
     * @param millis cooldown length in milliseconds
     * @return true at most once per {@code millis}
     */
    public boolean ready(long millis) {
        long now = System.currentTimeMillis();
        if (last != Long.MIN_VALUE && now - last <= millis) return false;
        last = now;
        return true;
    }
}
