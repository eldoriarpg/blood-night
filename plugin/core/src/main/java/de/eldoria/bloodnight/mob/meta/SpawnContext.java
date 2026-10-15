/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.function.Predicate;

/**
 * Where and how an entity spawned, as far as spawn rules care.
 *
 * @param world      the world's name
 * @param biome      whether the spawn is in a biome given by key ({@code minecraft:desert}) or tag
 *                   ({@code #minecraft:is_nether})
 * @param y          the block height
 * @param blockLight the light from blocks at the spawn, 0 to 15
 * @param skyVisible whether nothing above the spawn blocks the sky
 * @param reason     why the entity spawned
 * @param bloodNight whether a blood night runs in the world
 */
public record SpawnContext(
        String world,
        Predicate<String> biome,
        int y,
        int blockLight,
        boolean skyVisible,
        SpawnReason reason,
        boolean bloodNight) {}
