/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.regions;

import org.bukkit.Location;

/**
 * Whether the region at a place allows a part of blood nights.
 */
@FunctionalInterface
public interface RegionRules {
    /**
     * Allows everything everywhere, for servers without a region plugin.
     */
    RegionRules NONE = (location, flag) -> true;

    /**
     * @return whether the flag is allowed at the location
     */
    boolean allows(Location location, RegionFlag flag);
}
