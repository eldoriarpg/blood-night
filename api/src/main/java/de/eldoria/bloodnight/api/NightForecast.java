/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

/**
 * The next blood night of a world.
 *
 * @param night       the night's number
 * @param nightsAhead how many nights come before it: 0 when it is the coming or running one
 * @param ticksUntil  game ticks until it begins; 0 when it runs
 * @param forced      whether an admin forced it
 */
public record NightForecast(long night, long nightsAhead, long ticksUntil, boolean forced) {}
