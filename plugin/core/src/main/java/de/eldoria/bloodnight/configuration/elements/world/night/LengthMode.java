/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.night;

/**
 * How long a blood night lasts.
 */
public enum LengthMode {
    /**
     * As long as any night.
     */
    NORMAL,
    /**
     * A fixed length.
     */
    EXTENDED,
    /**
     * A length between two values, drawn for each night.
     */
    RANGE
}
