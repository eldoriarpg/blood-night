/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.server;

/**
 * How particle effects are limited: whether mob effects show at all, how far, and how many particles the server sends
 * for them each tick. Spawn Particle nodes in graphs are not affected.
 */
public class ParticleSettings {
    private boolean enabled = true;
    private boolean ambient = true;
    private int viewDistance = 32;
    private int budgetPerTick = 2000;
    private int perMob = 4;

    /**
     * @return whether particle effects play at all
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * @return whether the effects mobs wear while they live play
     */
    public boolean ambient() {
        return ambient;
    }

    /**
     * @return how far away in blocks players still see effects, 8 to 128
     */
    public int viewDistance() {
        return Math.clamp(viewDistance, 8, 128);
    }

    /**
     * @return how many particles all effects together send per tick, counted once per player who sees them; over it,
     *     the effects mobs wear skip first and those graphs play only past twice as many
     */
    public int budgetPerTick() {
        return Math.max(budgetPerTick, 0);
    }

    /**
     * @return how many effects one mob plays at once, the one it wears included
     */
    public int perMob() {
        return Math.max(perMob, 1);
    }
}
