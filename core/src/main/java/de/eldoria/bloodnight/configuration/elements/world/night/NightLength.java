/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.night;

import java.util.OptionalLong;
import java.util.SplittableRandom;

/**
 * The length of a blood night, in game seconds of 20 ticks, so it follows the server's tick rate. Lengths below one
 * second count as one second; a maximum below the minimum counts as the minimum.
 */
public class NightLength {
    private LengthMode mode = LengthMode.NORMAL;
    private int seconds = 600;
    private int maxSeconds = 900;

    /**
     * @return the mode
     */
    public LengthMode mode() {
        return mode;
    }

    /**
     * @param night a night's number, which picks the length in {@link LengthMode#RANGE} so it does not change when
     *              asked again
     * @return the length in ticks, or empty for {@link LengthMode#NORMAL}
     */
    public OptionalLong ticks(long night) {
        int min = Math.max(seconds, 1);
        return switch (mode) {
            case NORMAL -> OptionalLong.empty();
            case EXTENDED -> OptionalLong.of(min * 20L);
            case RANGE ->
                OptionalLong.of(new SplittableRandom(night).nextInt(min, Math.max(maxSeconds, min) + 1) * 20L);
        };
    }
}
