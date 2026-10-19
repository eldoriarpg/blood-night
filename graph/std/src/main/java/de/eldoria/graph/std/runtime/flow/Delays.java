/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The chains delay nodes hold back, per behaviour instance. The platform's clock hands them back once they are due;
 * they die with the instance, so a mob that is gone runs nothing later.
 */
public final class Delays {
    private final List<Pending> pending = new ArrayList<>();

    private record Pending(String key, long due, Runnable chain) {}

    /**
     * Runs a chain once a time has passed.
     *
     * @param millis how long to wait
     * @param chain  what runs then
     */
    public void after(long millis, Runnable chain) {
        pending.add(new Pending("", System.currentTimeMillis() + Math.max(0, millis), chain));
    }

    /**
     * Runs a chain once a time has passed, dropping a chain the same key still holds back, so the wait starts anew.
     *
     * @param key    who waits, e.g. the delay node's id
     * @param millis how long to wait
     * @param chain  what runs then
     */
    public void restart(String key, long millis, Runnable chain) {
        pending.removeIf(waiting -> waiting.key().equals(key));
        pending.add(new Pending(key, System.currentTimeMillis() + Math.max(0, millis), chain));
    }

    /**
     * Runs every chain that is due, in the order they were held back. A chain that waits again runs no earlier than
     * the next tick.
     */
    public void tick() {
        if (pending.isEmpty()) return;
        long now = System.currentTimeMillis();
        List<Pending> due =
                pending.stream().filter(waiting -> waiting.due() <= now).toList();
        pending.removeAll(due);
        due.forEach(waiting -> Objects.requireNonNull(waiting.chain()).run());
    }

    /**
     * @return how many chains are held back
     */
    public int waiting() {
        return pending.size();
    }
}
