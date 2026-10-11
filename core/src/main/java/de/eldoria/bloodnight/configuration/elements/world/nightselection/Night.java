/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection;

import java.time.Instant;

/**
 * A night of a world.
 *
 * @param number        the night's number: the day it begins on, {@code fullTime / 24000}
 * @param expectedStart when the night is expected to begin in real time; only the real moon phase looks at it
 */
public record Night(long number, Instant expectedStart) {}
