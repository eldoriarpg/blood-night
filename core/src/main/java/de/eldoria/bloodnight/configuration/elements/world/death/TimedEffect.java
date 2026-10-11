/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.death;

/**
 * A potion effect by its key, e.g. {@code minecraft:nausea}, lasting a number of seconds.
 */
public class TimedEffect {
    private String effect = "minecraft:nausea";
    private double seconds = 5;

    TimedEffect() {}

    TimedEffect(String effect, double seconds) {
        this.effect = effect;
        this.seconds = seconds;
    }

    public String effect() {
        return effect;
    }

    /**
     * @return how long the effect lasts, at least 0
     */
    public double seconds() {
        return Math.max(seconds, 0);
    }
}
