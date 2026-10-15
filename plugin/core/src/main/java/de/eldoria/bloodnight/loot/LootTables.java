/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The server's loot tables by id, kept in {@code loot_tables.yml}. Mobs, worlds and the server assign them by id.
 */
public class LootTables {
    @JsonProperty
    private final Map<String, LootTable> tables;

    @JsonCreator
    public LootTables(@JsonProperty("tables") @Nullable Map<String, LootTable> tables) {
        this.tables = new TreeMap<>(Objects.requireNonNullElse(tables, Map.of()));
    }

    public LootTables() {
        this(null);
    }

    /**
     * @param id a table's id
     * @return the table, or null when there is none with that id
     */
    public @Nullable LootTable get(String id) {
        return tables.get(id);
    }

    /**
     * @return every table by id
     */
    public Map<String, LootTable> tables() {
        return Collections.unmodifiableMap(tables);
    }
}
