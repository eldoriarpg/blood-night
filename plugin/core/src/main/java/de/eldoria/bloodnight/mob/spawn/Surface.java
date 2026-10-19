/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.spawn;

/**
 * Where a spawn is relative to the sky.
 */
public enum Surface {
    ANY,
    /**
     * Nothing above the spawn blocks the sky.
     */
    SKY_VISIBLE,
    /**
     * Something above the spawn blocks the sky.
     */
    UNDERGROUND
}
