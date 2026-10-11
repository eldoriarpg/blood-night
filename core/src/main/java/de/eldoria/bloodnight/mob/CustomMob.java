/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.SpawnRules;
import de.eldoria.graph.model.Graph;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A custom mob as configured: looks, attributes, drops, extension and the node graph of its behaviour.
 *
 * @param id        unique lower-case id
 * @param equipment item ids per slot; none keeps what the entity spawned with
 * @param attributes type, name, health and damage
 * @param behaviour the behaviour graph
 * @param mobDrops  drops on death
 * @param extension entity riding or carrying the mob
 * @param spawn     when the mob takes over spawning entities and when it goes again
 */
public record CustomMob(
        String id,
        @Nullable Equipment equipment,
        Attributes attributes,
        Graph behaviour,
        MobDrops mobDrops,
        Extension extension,
        SpawnRules spawn) {
    public CustomMob {
        Objects.requireNonNull(id, "Mob id can not be null");
        if (!id.equals(id.toLowerCase())) throw new IllegalArgumentException("Mob id must be lower case.");
        Objects.requireNonNull(behaviour, "Behaviour can not be null");
        Objects.requireNonNull(extension, "Extension can not be null");
        Objects.requireNonNull(spawn, "Spawn rules can not be null");
    }

    /**
     * Reads a mob; one without {@code behaviour} gets an empty graph, one without {@code spawn} the default rules.
     */
    @JsonCreator
    static CustomMob read(
            @JsonProperty("id") String id,
            @JsonProperty("equipment") @Nullable Equipment equipment,
            @JsonProperty("attributes") Attributes attributes,
            @JsonProperty("behaviour") @Nullable Graph behaviour,
            @JsonProperty("mobDrops") MobDrops mobDrops,
            @JsonProperty("extension") Extension extension,
            @JsonProperty("spawn") @Nullable SpawnRules spawn) {
        return new CustomMob(
                id,
                equipment,
                attributes,
                behaviour == null ? Graph.builder().build() : behaviour,
                mobDrops,
                extension,
                Objects.requireNonNullElseGet(spawn, SpawnRules::defaults));
    }
}
