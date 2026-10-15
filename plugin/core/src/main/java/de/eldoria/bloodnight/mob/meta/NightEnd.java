/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

/**
 * What happens to a custom mob when the blood night it spawned in ends. Mobs spawned outside a blood night stay as
 * they are.
 */
public enum NightEnd {
    /**
     * The mob goes away.
     */
    REMOVE,
    /**
     * The mob stays as it is, with its behaviour.
     */
    KEEP,
    /**
     * The mob stays as an ordinary mob: no behaviour, name, extension or attribute changes, but its equipment.
     */
    REVERT
}
