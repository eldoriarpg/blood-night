/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

import java.util.OptionalLong;

/**
 * How a world has blood nights.
 *
 * @param enabled          whether the world has blood nights at all
 * @param timed            whether its time stands still, e.g. the Nether and the End, so blood nights run by the
 *                         clock instead of at night
 * @param beginTick        the time of day a blood night begins, in ticks; empty for a timed world
 * @param endTick          the time of day it ends, in ticks; empty for a timed world
 * @param selection        how nights are picked, e.g. {@code RANDOM} or {@code MOON_PHASE}; new kinds may appear
 * @param healthMultiplier how much tougher ordinary enemies are during blood nights; damage players deal to them is
 *                         divided by it
 * @param damageModifier   how much harder ordinary enemies hit during blood nights, as a factor
 */
public record WorldInfo(
        boolean enabled,
        boolean timed,
        OptionalLong beginTick,
        OptionalLong endTick,
        String selection,
        double healthMultiplier,
        double damageModifier) {}
