/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightProbability;

/**
 * A probability that follows a smooth curve over {@code period} nights: {@code min} on the first night, {@code max}
 * halfway through, back to {@code min} at the end. A period below 2 counts as 2.
 */
public class Curve implements NightProbability {
    private int min = 20;
    private int max = 80;
    private int period = 10;

    @Override
    public int probability(Night night) {
        int length = Math.max(period, 2);
        double position = Math.floorMod(night.number(), (long) length);
        double half = length / 2.0;
        double value = position <= half
                ? smooth(min, max, position / half)
                : smooth(max, min, (position - half) / (length - half));
        return NightProbability.percent((int) value);
    }

    /**
     * Cosine interpolation: starts and ends flat, steepest in the middle.
     */
    private static double smooth(double from, double to, double progress) {
        return from + (to - from) * (1 - Math.cos(Math.PI * progress)) / 2;
    }
}
