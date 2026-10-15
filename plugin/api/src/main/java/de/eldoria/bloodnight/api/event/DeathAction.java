/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

/**
 * Something that happens where a player or an enemy dies during a blood night.
 */
public enum DeathAction {
    /** A lightning strike without damage. */
    LIGHTNING,
    /** Thunder heard in the whole world. */
    THUNDER,
    /** A shockwave pushing players away and giving them effects. */
    SHOCKWAVE
}
