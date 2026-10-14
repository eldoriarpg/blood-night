/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

/**
 * What happens to the vanilla loot of a special mob a player kills. Equipment it wears or holds is never touched.
 */
public enum VanillaLoot {
    /** The loot stays as it is. */
    KEEP,
    /** The loot is multiplied like that of ordinary enemies in the world. */
    MULTIPLY,
    /** The mob drops no vanilla loot. */
    CLEAR
}
