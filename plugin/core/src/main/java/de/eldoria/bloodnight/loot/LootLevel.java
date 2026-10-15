/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One level that assigns loot to a kill: a mob, its world or the server.
 *
 * @param tables      the ids of the tables it assigns
 * @param slots       how many entries drop, as weights by count (0 is nothing); null leaves it to a broader level
 * @param replaceSame whether its items replace the same items of the broader levels
 */
public record LootLevel(List<String> tables, @Nullable Map<Integer, Integer> slots, boolean replaceSame) {
    public LootLevel {
        tables = List.copyOf(Objects.requireNonNullElse(tables, List.of()));
        slots = slots == null ? null : Map.copyOf(slots);
    }
}
