/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

/**
 * How many particles effects may still send this tick, counted once per player who sees them. The effects mobs wear
 * stop at the budget; effects graphs play go on to twice as much, so a boss's attack still shows in a crowded fight.
 * <p>
 * The count is emptied by a task on the global region every tick. On Folia regions tick on their own threads, so the
 * budget is shared by all of them and only roughly per tick.
 */
public final class Budget {
    private final AtomicInteger used = new AtomicInteger();
    private final IntSupplier perTick;

    /**
     * @param perTick the budget per tick, read again every time
     */
    public Budget(IntSupplier perTick) {
        this.perTick = perTick;
    }

    /**
     * Takes particles from the budget if there are enough left.
     *
     * @param particles how many, already multiplied by the players who see them
     * @param worn      whether a mob wears the effect, which gets only the plain budget
     * @return whether they may be sent
     */
    public boolean take(int particles, boolean worn) {
        int limit = worn ? perTick.getAsInt() : perTick.getAsInt() * 2;
        int before = used.getAndAdd(particles);
        if (before + particles <= limit) return true;
        used.addAndGet(-particles);
        return false;
    }

    /**
     * Starts a new tick.
     */
    public void reset() {
        used.set(0);
    }
}
