/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.spawn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Where a custom mob may spawn. Empty lists and missing bounds allow everything.
 *
 * @param worlds   world names
 * @param biomes   biome keys ({@code minecraft:desert}) or tags ({@code #minecraft:is_nether})
 * @param minY     lowest block height
 * @param maxY     highest block height
 * @param maxLight brightest light from blocks, 0 to 15
 * @param surface  relation to the sky
 */
public record SpawnConditions(
        List<String> worlds,
        List<String> biomes,
        @Nullable Integer minY,
        @Nullable Integer maxY,
        @Nullable Integer maxLight,
        Surface surface) {
    public SpawnConditions {
        worlds = List.copyOf(worlds);
        biomes = List.copyOf(biomes);
        Objects.requireNonNull(surface, "Surface can not be null");
    }

    @JsonCreator
    static SpawnConditions read(
            @JsonProperty("worlds") @Nullable List<String> worlds,
            @JsonProperty("biomes") @Nullable List<String> biomes,
            @JsonProperty("minY") @Nullable Integer minY,
            @JsonProperty("maxY") @Nullable Integer maxY,
            @JsonProperty("maxLight") @Nullable Integer maxLight,
            @JsonProperty("surface") @Nullable Surface surface) {
        return new SpawnConditions(
                Objects.requireNonNullElse(worlds, List.of()),
                Objects.requireNonNullElse(biomes, List.of()),
                minY,
                maxY,
                maxLight,
                Objects.requireNonNullElse(surface, Surface.ANY));
    }

    /**
     * @return conditions allowing every spawn
     */
    public static SpawnConditions any() {
        return new SpawnConditions(List.of(), List.of(), null, null, null, Surface.ANY);
    }

    /**
     * @param context the spawn
     * @return whether the spawn meets every condition
     */
    public boolean allows(SpawnContext context) {
        if (!worlds.isEmpty() && worlds.stream().noneMatch(context.world()::equalsIgnoreCase)) return false;
        if (!biomes.isEmpty() && biomes.stream().noneMatch(context.biome())) return false;
        if (minY != null && context.y() < minY) return false;
        if (maxY != null && context.y() > maxY) return false;
        if (maxLight != null && context.blockLight() > maxLight) return false;
        return switch (surface) {
            case ANY -> true;
            case SKY_VISIBLE -> context.skyVisible();
            case UNDERGROUND -> !context.skyVisible();
        };
    }
}
