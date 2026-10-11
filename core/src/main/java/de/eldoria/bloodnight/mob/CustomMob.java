/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.graph.LegacyGraphs;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.graph.model.Graph;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;

/**
 * A custom mob as configured: looks, attributes, drops, extension and the node graph of its behaviour.
 * <p>
 * Configs written by the old object runtime store the behaviour under {@code nodes}; such mobs are migrated when read,
 * and what could not be carried over is kept in {@link #migrationNotes()} until the config is saved again.
 *
 * @param id             unique lower-case id
 * @param equipment      item ids per slot
 * @param attributes     type, name, health and damage
 * @param behaviour      the behaviour graph
 * @param mobDrops       drops on death
 * @param extension      entity riding or carrying the mob
 * @param migrationNotes problems found while migrating an old behaviour; never written
 */
public record CustomMob(
        String id,
        Equipment equipment,
        Attributes attributes,
        Graph behaviour,
        MobDrops mobDrops,
        Extension extension,
        @JsonIgnore List<String> migrationNotes) {
    public CustomMob {
        Objects.requireNonNull(id, "Mob id can not be null");
        if (!id.equals(id.toLowerCase())) throw new IllegalArgumentException("Mob id must be lower case.");
        Objects.requireNonNull(extension, "Extension can not be null");
        behaviour = behaviour == null ? Graph.builder().build() : behaviour;
        migrationNotes = migrationNotes == null ? List.of() : List.copyOf(migrationNotes);
    }

    public CustomMob(
            String id,
            Equipment equipment,
            Attributes attributes,
            Graph behaviour,
            MobDrops mobDrops,
            Extension extension) {
        this(id, equipment, attributes, behaviour, mobDrops, extension, List.of());
    }

    /**
     * Reads a mob, migrating an old behaviour stored under {@code nodes} when no {@code behaviour} is present.
     */
    @JsonCreator
    static CustomMob read(
            @JsonProperty("id") String id,
            @JsonProperty("equipment") Equipment equipment,
            @JsonProperty("attributes") Attributes attributes,
            @JsonProperty("behaviour") Graph behaviour,
            @JsonProperty("nodes") JsonNode legacy,
            @JsonProperty("mobDrops") MobDrops mobDrops,
            @JsonProperty("extension") Extension extension) {
        if (behaviour == null && legacy != null && !legacy.isNull()) {
            LegacyGraphs.Migration migration = LegacyGraphs.migrate(legacy);
            return new CustomMob(
                    id, equipment, attributes, migration.graph(), mobDrops, extension, migration.warnings());
        }
        return new CustomMob(id, equipment, attributes, behaviour, mobDrops, extension, List.of());
    }
}
