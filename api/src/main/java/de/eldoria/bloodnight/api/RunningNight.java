/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

/**
 * A blood night that runs in a world.
 *
 * @param night        the night's number
 * @param ticksLeft    game ticks until it ends
 * @param shareLeft    how much of it is still to come, from 1 at its begin to 0 at its end
 * @param held         whether something holds it beyond its time, such as a dragon fight
 */
public record RunningNight(long night, long ticksLeft, double shareLeft, boolean held) {}
