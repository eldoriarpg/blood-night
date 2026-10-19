/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime.world;

/**
 * Who hears a broadcast from a mob.
 */
public enum Reach {
    /** The players in the mob's world. */
    WORLD,
    /** Every player on the server. */
    SERVER
}
