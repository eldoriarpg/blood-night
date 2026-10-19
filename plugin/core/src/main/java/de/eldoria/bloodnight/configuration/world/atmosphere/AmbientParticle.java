/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.atmosphere;

/**
 * A particle drifting around a player during a blood night, by its key, e.g. {@code minecraft:ash}.
 */
public class AmbientParticle {
    private String particle = "minecraft:ash";
    private int perSecond = 10;

    AmbientParticle() {}

    public AmbientParticle(String particle, int perSecond) {
        this.particle = particle;
        this.perSecond = perSecond;
    }

    public String particle() {
        return particle;
    }

    /**
     * @return how many appear around the player each second, at least 0
     */
    public int perSecond() {
        return Math.max(perSecond, 0);
    }
}
