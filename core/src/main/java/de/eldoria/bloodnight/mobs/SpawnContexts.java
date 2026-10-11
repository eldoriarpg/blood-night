/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.mob.meta.SpawnContext;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Objects;

/**
 * Describes where an entity spawned for spawn rules.
 */
final class SpawnContexts {
    private SpawnContexts() {}

    /**
     * @param entity     the spawned entity
     * @param bloodNight whether a blood night runs in its world
     * @return the spawn
     */
    static SpawnContext of(LivingEntity entity, boolean bloodNight) {
        Block block = entity.getLocation().getBlock();
        Biome biome = block.getBiome();
        return new SpawnContext(
                entity.getWorld().getName(),
                name -> inBiome(biome, name),
                block.getY(),
                block.getLightFromBlocks(),
                entity.getWorld().getHighestBlockYAt(block.getX(), block.getZ()) <= block.getY(),
                Objects.requireNonNullElse(entity.getEntitySpawnReason(), SpawnReason.DEFAULT),
                bloodNight);
    }

    private static boolean inBiome(Biome biome, String name) {
        if (!name.startsWith("#"))
            return biome.key().asString().equals(Key.key(name).asString());
        var registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        TagKey<Biome> tag = TagKey.create(RegistryKey.BIOME, Key.key(name.substring(1)));
        if (!registry.hasTag(tag)) return false;
        return registry.getTag(tag).contains(TypedKey.create(RegistryKey.BIOME, biome.key()));
    }
}
