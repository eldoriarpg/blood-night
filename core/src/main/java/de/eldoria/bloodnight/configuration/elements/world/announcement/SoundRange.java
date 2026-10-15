/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.announcement;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A range a sound's pitch or volume is picked from each time it plays. {@code min} and {@code max} may be swapped;
 * equal, the value is always the same.
 */
public class SoundRange {
    private float min = 1f;
    private float max = 1f;

    SoundRange() {}

    SoundRange(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float min() {
        return min;
    }

    public float max() {
        return max;
    }

    /**
     * @param lowest  the least value allowed
     * @param highest the greatest value allowed
     * @return a value between {@code min} and {@code max}, within the allowed values
     */
    float pick(float lowest, float highest) {
        float from = Math.min(min, max);
        float to = Math.max(min, max);
        float value = from == to ? from : from + ThreadLocalRandom.current().nextFloat() * (to - from);
        return Math.clamp(value, lowest, highest);
    }
}
