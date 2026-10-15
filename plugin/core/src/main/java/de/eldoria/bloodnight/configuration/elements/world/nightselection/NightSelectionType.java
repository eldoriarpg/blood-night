/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection;

/**
 * The ways a world decides which nights are blood nights, as the alpha had them.
 */
public enum NightSelectionType {
    /**
     * The same probability every night.
     */
    RANDOM,
    /**
     * A probability per phase of the world's moon.
     */
    MOON_PHASE,
    /**
     * A probability per phase of the real moon on the night's date.
     */
    REAL_MOON_PHASE,
    /**
     * Every n-th night, with a probability.
     */
    INTERVAL,
    /**
     * A custom cycle of phases, each with its own probability.
     */
    PHASE,
    /**
     * A probability rising smoothly from a minimum to a maximum and back over a period of nights.
     */
    CURVE
}
