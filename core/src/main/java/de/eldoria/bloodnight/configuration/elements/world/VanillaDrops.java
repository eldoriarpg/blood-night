/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.mob.meta.ValueModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.EnumSet;
import java.util.Set;

/**
 * What ordinary enemies a player kills during a blood night drop. Equipment they wear or hold is never multiplied,
 * only their loot.
 *
 * @param enabled         whether their drops change at all
 * @param lootMultiplier  how much of their loot falls, e.g. 2 for twice the amount; stacks stay within their size
 * @param extraDrops      whether they also roll the world's drops, like special mobs without drops of their own
 * @param xpModifier      how their experience changes
 * @param xpValue         the number it changes by
 * @param excludedTypes   kinds of mobs whose drops never change
 * @param excludedReasons mobs spawned for these reasons keep their drops, e.g. from spawners or through portals
 */
public record VanillaDrops(
        boolean enabled,
        double lootMultiplier,
        boolean extraDrops,
        ValueModifier xpModifier,
        double xpValue,
        Set<EntityType> excludedTypes,
        Set<SpawnReason> excludedReasons) {
    public static VanillaDrops defaultDrops() {
        return new VanillaDrops(
                true,
                2,
                false,
                ValueModifier.MULTIPLY,
                2,
                EnumSet.noneOf(EntityType.class),
                EnumSet.of(SpawnReason.SPAWNER, SpawnReason.TRIAL_SPAWNER, SpawnReason.NETHER_PORTAL));
    }
}
