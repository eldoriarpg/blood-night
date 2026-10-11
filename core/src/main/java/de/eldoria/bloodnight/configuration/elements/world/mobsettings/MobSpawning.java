/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.mobsettings;

import de.eldoria.bloodnight.mob.CustomMob;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * How entities spawning in a world become custom mobs.
 * <p>
 * Every custom mob may spawn in every enabled world by its own spawn rules. {@code spawnPercentage} is the chance a
 * spawn that some custom mob fits becomes one; which mob is picked by weight. {@code mobs} overrides the weight of a
 * mob in this world by its id; 0 keeps it out of the world.
 */
public class MobSpawning {
    private int spawnPercentage = 50;
    /**
     * Mobs spawned for these reasons, e.g. {@code SPAWNER} or {@code TRIAL_SPAWNER}, never become special mobs and
     * cause no death actions.
     */
    private Set<SpawnReason> ignoredReasons = EnumSet.noneOf(SpawnReason.class);

    private Map<String, Integer> mobs = new HashMap<>();

    /**
     * @return the chance in percent a fitting spawn becomes a custom mob, 0 to 100
     */
    public int spawnPercentage() {
        return Math.clamp(spawnPercentage, 0, 100);
    }

    public Set<SpawnReason> ignoredReasons() {
        return ignoredReasons;
    }

    /**
     * @param mob a custom mob
     * @return the mob's weight in this world, at least 0
     */
    public int weight(CustomMob mob) {
        return Math.max(mobs.getOrDefault(mob.id(), mob.spawn().weight()), 0);
    }
}
