/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * When a custom mob takes over a spawning entity of its type, and when it goes again.
 *
 * @param weight     how often the mob is picked relative to the other mobs fitting a spawn; 0 never spawns it
 *                   naturally. Worlds may override it.
 * @param nightOnly  whether the mob only spawns during blood nights
 * @param despawn    when the mob is removed again
 * @param reasons    the spawn reasons the mob takes; empty takes all
 * @param conditions where the mob may spawn
 */
public record SpawnRules(
        int weight, boolean nightOnly, Despawn despawn, Set<SpawnReason> reasons, SpawnConditions conditions) {
    public static final int DEFAULT_WEIGHT = 10;

    public SpawnRules {
        weight = Math.max(weight, 0);
        Objects.requireNonNull(despawn, "Despawn can not be null");
        reasons = Set.copyOf(reasons);
        Objects.requireNonNull(conditions, "Conditions can not be null");
    }

    @JsonCreator
    static SpawnRules read(
            @JsonProperty("weight") @Nullable Integer weight,
            @JsonProperty("nightOnly") @Nullable Boolean nightOnly,
            @JsonProperty("despawn") @Nullable Despawn despawn,
            @JsonProperty("reasons") @Nullable Set<SpawnReason> reasons,
            @JsonProperty("conditions") @Nullable SpawnConditions conditions) {
        return new SpawnRules(
                Objects.requireNonNullElse(weight, DEFAULT_WEIGHT),
                Objects.requireNonNullElse(nightOnly, true),
                Objects.requireNonNullElse(despawn, Despawn.NIGHT_END),
                Objects.requireNonNullElse(reasons, Set.of()),
                Objects.requireNonNullElseGet(conditions, SpawnConditions::any));
    }

    /**
     * @return rules spawning the mob anywhere during blood nights and removing it at their end
     */
    public static SpawnRules defaults() {
        return new SpawnRules(DEFAULT_WEIGHT, true, Despawn.NIGHT_END, Set.of(), SpawnConditions.any());
    }

    /**
     * @param context the spawn
     * @return whether the mob may take the spawn, weight aside
     */
    public boolean allows(SpawnContext context) {
        if (nightOnly && !context.bloodNight()) return false;
        if (!reasons.isEmpty() && !reasons.contains(context.reason())) return false;
        return conditions.allows(context);
    }
}
