/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

/**
 * When a custom mob goes again.
 */
public enum Despawn {
    /**
     * Removed when the blood night it spawned in ends; mobs spawned outside a blood night stay.
     */
    NIGHT_END,
    /**
     * Stays like any other mob.
     */
    NEVER
}
