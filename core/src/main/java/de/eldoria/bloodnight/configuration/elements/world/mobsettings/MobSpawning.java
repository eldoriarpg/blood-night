/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.mobsettings;

import java.util.Set;

public class MobSpawning {
    /**
     * The percentage of wrapped mobs
     */
    int spawnPercentage = 50;
    /**
     * The mobs which are permitted to spawn.
     */
    private Set<String> activeTypes;

    public Set<String> activeTypes() {
        return activeTypes;
    }

    public int spawnPercentage() {
        return spawnPercentage;
    }
}
