/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.mobsettings;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.EnumSet;
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
    /**
     * Mobs spawned for these reasons, e.g. {@code SPAWNER} or {@code TRIAL_SPAWNER}, never become special mobs and
     * cause no death actions.
     */
    private Set<SpawnReason> ignoredReasons = EnumSet.noneOf(SpawnReason.class);

    public Set<String> activeTypes() {
        return activeTypes;
    }

    public Set<SpawnReason> ignoredReasons() {
        return ignoredReasons;
    }

    public int spawnPercentage() {
        return spawnPercentage;
    }
}
