/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.announcement;

/**
 * A potion effect players get when a blood night begins or ends around them, by its key, e.g.
 * {@code minecraft:darkness}.
 */
public class NightEffect {
    private boolean enabled = true;
    private String effect = "minecraft:darkness";
    private int seconds = 5;

    public boolean enabled() {
        return enabled;
    }

    public String effect() {
        return effect;
    }

    /**
     * @return how long the effect lasts, at least one second
     */
    public int seconds() {
        return Math.max(seconds, 1);
    }
}
