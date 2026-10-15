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
import de.eldoria.bloodnight.mob.meta.MobOrigin;
import de.eldoria.bloodnight.mob.meta.Movement;
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
 * @param origin    the store entry the mob was installed from; {@code null} for the user's own mobs
 * @param movement  how it moves on top of its vanilla AI
 */
public record CustomMob(
        String id,
        @Nullable Equipment equipment,
        Attributes attributes,
        Graph behaviour,
        MobDrops mobDrops,
        Extension extension,
        SpawnRules spawn,
        @Nullable MobOrigin origin,
        Movement movement) {
    public CustomMob {
        Objects.requireNonNull(id, "Mob id can not be null");
        if (!id.equals(id.toLowerCase())) throw new IllegalArgumentException("Mob id must be lower case.");
        Objects.requireNonNull(behaviour, "Behaviour can not be null");
        Objects.requireNonNull(extension, "Extension can not be null");
        Objects.requireNonNull(spawn, "Spawn rules can not be null");
        movement = Movement.orVanilla(movement);
    }

    /**
     * A mob that moves as its vanilla entity does.
     */
    public CustomMob(
            String id,
            @Nullable Equipment equipment,
            Attributes attributes,
            Graph behaviour,
            MobDrops mobDrops,
            Extension extension,
            SpawnRules spawn,
            @Nullable MobOrigin origin) {
        this(id, equipment, attributes, behaviour, mobDrops, extension, spawn, origin, Movement.VANILLA);
    }

    /**
     * A mob of the user's own, not from a store.
     */
    public CustomMob(
            String id,
            @Nullable Equipment equipment,
            Attributes attributes,
            Graph behaviour,
            MobDrops mobDrops,
            Extension extension,
            SpawnRules spawn) {
        this(id, equipment, attributes, behaviour, mobDrops, extension, spawn, null);
    }

    /**
     * @return the same mob with other attributes
     */
    public CustomMob withAttributes(Attributes attributes) {
        return new CustomMob(id, equipment, attributes, behaviour, mobDrops, extension, spawn, origin, movement);
    }

    /**
     * @return the same mob with another origin
     */
    public CustomMob withOrigin(@Nullable MobOrigin origin) {
        return new CustomMob(id, equipment, attributes, behaviour, mobDrops, extension, spawn, origin, movement);
    }

    /**
     * @return the same mob, tuning and origin kept, with another behaviour, extension and movement
     */
    public CustomMob withLogic(Graph behaviour, Extension extension, Movement movement) {
        return new CustomMob(id, equipment, attributes, behaviour, mobDrops, extension, spawn, origin, movement);
    }

    /**
     * @return the same mob moving another way
     */
    public CustomMob withMovement(Movement movement) {
        return new CustomMob(id, equipment, attributes, behaviour, mobDrops, extension, spawn, origin, movement);
    }

    /**
     * Reads a mob; one without {@code behaviour} gets an empty graph, one without {@code spawn} the default rules, one
     * without {@code movement} the vanilla one.
     */
    @JsonCreator
    static CustomMob read(
            @JsonProperty("id") String id,
            @JsonProperty("equipment") @Nullable Equipment equipment,
            @JsonProperty("attributes") Attributes attributes,
            @JsonProperty("behaviour") @Nullable Graph behaviour,
            @JsonProperty("mobDrops") MobDrops mobDrops,
            @JsonProperty("extension") Extension extension,
            @JsonProperty("spawn") @Nullable SpawnRules spawn,
            @JsonProperty("origin") @Nullable MobOrigin origin,
            @JsonProperty("movement") @Nullable Movement movement) {
        return new CustomMob(
                id,
                equipment,
                attributes,
                behaviour == null ? Graph.builder().build() : behaviour,
                mobDrops,
                extension,
                Objects.requireNonNullElseGet(spawn, SpawnRules::defaults),
                origin,
                Movement.orVanilla(movement));
    }
}
