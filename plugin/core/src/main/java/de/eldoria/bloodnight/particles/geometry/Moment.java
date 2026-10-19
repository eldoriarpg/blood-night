/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

/**
 * When in a play a shape is drawn, for the shapes that change over time.
 *
 * @param progress how far the play is, from 0 at its start to 1 at its end
 * @param emission how many emissions of this emitter came before, for spinning
 * @param tick     ticks since the play started, for flapping
 */
public record Moment(double progress, long emission, long tick) {
    /** The start of a play. */
    public static final Moment START = new Moment(0, 0, 0);

    public Moment {
        progress = Math.clamp(progress, 0, 1);
    }
}
