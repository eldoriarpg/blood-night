/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.model.Macro;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * What comes back from the editor: mobs, macros, items, loot tables, settings and the file hashes taken when the session opened.
 * Problems, the server's choices and the catalog version in the payload are ignored.
 *
 * @param mobs     mob definitions by id
 * @param macros   the macro library
 * @param items    the items by id
 * @param lootTables the loot tables by id
 * @param settings the settings, if the payload has them
 * @param hashes   the hashes of the files when the session opened; empty when the payload lost them
 */
public record EditorResult(
        Map<String, CustomMob> mobs,
        List<Macro> macros,
        Map<String, EditorItem> items,
        Map<String, LootTable> lootTables,
        @Nullable SettingsPayload settings,
        Map<String, String> hashes) {
    @JsonCreator
    static EditorResult read(
            @JsonProperty("mobs") @Nullable Map<String, CustomMob> mobs,
            @JsonProperty("macros") @Nullable List<Macro> macros,
            @JsonProperty("items") @Nullable Map<String, EditorItem> items,
            @JsonProperty("lootTables") @Nullable Map<String, LootTable> lootTables,
            @JsonProperty("settings") @Nullable SettingsPayload settings,
            @JsonProperty("hashes") @Nullable Map<String, String> hashes) {
        return new EditorResult(
                Objects.requireNonNullElse(mobs, Map.of()),
                Objects.requireNonNullElse(macros, List.of()),
                Objects.requireNonNullElse(items, Map.of()),
                Objects.requireNonNullElse(lootTables, Map.of()),
                settings,
                Objects.requireNonNullElse(hashes, Map.of()));
    }

    /**
     * @param payload an edited payload
     * @return what it holds
     * @throws tools.jackson.core.JacksonException when it cannot be read
     */
    public static EditorResult of(JsonNode payload) {
        return EditorPayload.MAPPER.treeToValue(payload, EditorResult.class);
    }
}
