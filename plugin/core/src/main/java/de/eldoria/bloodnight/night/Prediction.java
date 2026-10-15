/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

/**
 * The next blood night of a world.
 *
 * @param night      its number
 * @param ticksUntil game ticks until it begins, 0 when it is running
 * @param forced     whether an admin forced it rather than the selection choosing it
 */
public record Prediction(long night, long ticksUntil, boolean forced) {}
