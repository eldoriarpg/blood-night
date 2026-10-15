/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.death;

import java.util.ArrayList;
import java.util.List;

/**
 * A shockwave from a death that pushes entities within {@code range} blocks away and gives living ones the effects.
 * Push and effect length fall off with the distance; {@code power} 10 is a normal push; effects last at least
 * {@code minSeconds}.
 */
public class ShockwaveSettings {
    private int chance = 10;
    private double power = 10;
    private double range = 10;
    private double minSeconds = 0.1;
    private List<TimedEffect> effects = new ArrayList<>(List.of(new TimedEffect("minecraft:nausea", 5)));

    /**
     * @return the chance of a shockwave, 0 to 100
     */
    public int chance() {
        return Math.clamp(chance, 0, 100);
    }

    /**
     * @return the push strength, at least 0
     */
    public double power() {
        return Math.max(power, 0);
    }

    /**
     * @return the radius in blocks, at least 0
     */
    public double range() {
        return Math.max(range, 0);
    }

    public double minSeconds() {
        return Math.max(minSeconds, 0);
    }

    public List<TimedEffect> effects() {
        return effects;
    }
}
