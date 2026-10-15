/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import java.util.List;
import java.util.Objects;

/**
 * Items that can drop, each with its chance and amount. How many of them drop at a kill is set where the table is
 * assigned, not on the table (see {@link LootLevel}).
 *
 * @param name    the name shown in the editor
 * @param entries the items
 */
public record LootTable(String name, List<LootEntry> entries) {
    public LootTable {
        name = Objects.requireNonNullElse(name, "");
        entries = List.copyOf(Objects.requireNonNullElse(entries, List.of()));
    }
}
