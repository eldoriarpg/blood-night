/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.regions;

/**
 * What a region can switch off for blood nights. Every flag allows by default.
 */
public enum RegionFlag {
    /** Mobs turn into special mobs. */
    CUSTOM_MOBS("bloodnight-custom-mobs"),
    /** Harder hits, tougher enemies, death actions and more loot. */
    RULES("bloodnight-rules"),
    /** Commands blocked during blood nights are blocked. */
    BLOCKED_COMMANDS("bloodnight-blocked-commands"),
    /** The begin and end effect, the boss bar and the atmosphere around the player. */
    EFFECTS("bloodnight-effects");

    private final String flagName;

    RegionFlag(String flagName) {
        this.flagName = flagName;
    }

    /**
     * @return the flag's name in the region plugin, e.g. {@code bloodnight-custom-mobs}
     */
    public String flagName() {
        return flagName;
    }
}
