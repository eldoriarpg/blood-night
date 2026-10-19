/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.loot.assign.MobLoot;
import de.eldoria.bloodnight.mob.attributes.Attributes;
import de.eldoria.bloodnight.mob.attributes.Movement;
import de.eldoria.bloodnight.mob.equipment.Equipment;
import de.eldoria.bloodnight.mob.equipment.Extension;
import de.eldoria.bloodnight.mob.spawn.SpawnRules;
import de.eldoria.graph.model.Graph;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A custom mob as configured: looks, attributes, drops, extension, the node graph of its behaviour and the particle
 * effects it wears.
 *
 * @param id        unique lower-case id
 * @param equipment item ids per slot; none keeps what the entity spawned with
 * @param attributes type, name, health and damage
 * @param behaviour the behaviour graph
 * @param loot      what it drops when a player kills it; none inherits its world's and the server's loot
 * @param extension entity riding or carrying the mob
 * @param spawn     when the mob takes over spawning entities and when it goes again
 * @param origin    the store entry the mob was installed from; {@code null} for the user's own mobs
 * @param movement  how it moves on top of its vanilla AI
 * @param particles the particle effects it wears without a graph; none wears none
 */
public record CustomMob(
        String id,
        @Nullable Equipment equipment,
        Attributes attributes,
        Graph behaviour,
        MobLoot loot,
        Extension extension,
        SpawnRules spawn,
        @Nullable MobOrigin origin,
        Movement movement,
        @Nullable MobParticles particles) {
    public CustomMob {
        Objects.requireNonNull(id, "Mob id can not be null");
        if (!id.equals(id.toLowerCase())) throw new IllegalArgumentException("Mob id must be lower case.");
        Objects.requireNonNull(behaviour, "Behaviour can not be null");
        Objects.requireNonNull(extension, "Extension can not be null");
        Objects.requireNonNull(spawn, "Spawn rules can not be null");
        loot = Objects.requireNonNullElseGet(loot, MobLoot::inherited);
        movement = Movement.orVanilla(movement);
    }

    /**
     * A mob without particle effects.
     */
    public CustomMob(
            String id,
            @Nullable Equipment equipment,
            Attributes attributes,
            Graph behaviour,
            MobLoot loot,
            Extension extension,
            SpawnRules spawn,
            @Nullable MobOrigin origin,
            Movement movement) {
        this(id, equipment, attributes, behaviour, loot, extension, spawn, origin, movement, null);
    }

    /**
     * A mob that moves as its vanilla entity does.
     */
    public CustomMob(
            String id,
            @Nullable Equipment equipment,
            Attributes attributes,
            Graph behaviour,
            MobLoot loot,
            Extension extension,
            SpawnRules spawn,
            @Nullable MobOrigin origin) {
        this(id, equipment, attributes, behaviour, loot, extension, spawn, origin, Movement.VANILLA);
    }

    /**
     * A mob of the user's own, not from a store.
     */
    public CustomMob(
            String id,
            @Nullable Equipment equipment,
            Attributes attributes,
            Graph behaviour,
            MobLoot loot,
            Extension extension,
            SpawnRules spawn) {
        this(id, equipment, attributes, behaviour, loot, extension, spawn, null);
    }

    /**
     * @return the same mob with other attributes
     */
    public CustomMob withAttributes(Attributes attributes) {
        return new CustomMob(id, equipment, attributes, behaviour, loot, extension, spawn, origin, movement, particles);
    }

    /**
     * @return the same mob with other loot settings
     */
    public CustomMob withLoot(MobLoot loot) {
        return new CustomMob(id, equipment, attributes, behaviour, loot, extension, spawn, origin, movement, particles);
    }

    /**
     * @return the same mob with another origin
     */
    public CustomMob withOrigin(@Nullable MobOrigin origin) {
        return new CustomMob(id, equipment, attributes, behaviour, loot, extension, spawn, origin, movement, particles);
    }

    /**
     * @return the same mob, tuning and origin kept, with another behaviour, extension and movement
     */
    public CustomMob withLogic(Graph behaviour, Extension extension, Movement movement) {
        return new CustomMob(id, equipment, attributes, behaviour, loot, extension, spawn, origin, movement, particles);
    }

    /**
     * @return the same mob moving another way
     */
    public CustomMob withMovement(Movement movement) {
        return new CustomMob(id, equipment, attributes, behaviour, loot, extension, spawn, origin, movement, particles);
    }

    /**
     * @return the same mob wearing other particle effects
     */
    public CustomMob withParticles(@Nullable MobParticles particles) {
        return new CustomMob(id, equipment, attributes, behaviour, loot, extension, spawn, origin, movement, particles);
    }

    /**
     * Reads a mob; one without {@code behaviour} gets an empty graph, one without {@code loot} inherits its world's and
     * the server's, one without {@code spawn} the default rules, one without {@code movement} the vanilla one.
     */
    @JsonCreator
    static CustomMob read(
            @JsonProperty("id") String id,
            @JsonProperty("equipment") @Nullable Equipment equipment,
            @JsonProperty("attributes") Attributes attributes,
            @JsonProperty("behaviour") @Nullable Graph behaviour,
            @JsonProperty("loot") @Nullable MobLoot loot,
            @JsonProperty("extension") Extension extension,
            @JsonProperty("spawn") @Nullable SpawnRules spawn,
            @JsonProperty("origin") @Nullable MobOrigin origin,
            @JsonProperty("movement") @Nullable Movement movement,
            @JsonProperty("particles") @Nullable MobParticles particles) {
        return new CustomMob(
                id,
                equipment,
                attributes,
                behaviour == null ? Graph.builder().build() : behaviour,
                Objects.requireNonNullElseGet(loot, MobLoot::inherited),
                extension,
                Objects.requireNonNullElseGet(spawn, SpawnRules::defaults),
                origin,
                Movement.orVanilla(movement),
                particles);
    }
}
