/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import java.util.Objects;

/**
 * One item a loot table can drop.
 *
 * @param item       a registered item's or an editor item's id, or a material key such as {@code minecraft:bone}
 * @param weight     how likely it is picked for a slot against every other entry in the pool; 0 never drops it
 * @param amount     how many drop when it is picked
 * @param repeatable whether it may be picked for more than one slot of the same kill
 */
public record LootEntry(String item, int weight, LootAmount amount, boolean repeatable) {
    public LootEntry {
        Objects.requireNonNull(item, "A loot entry needs an item");
        weight = Math.max(0, weight);
        amount = Objects.requireNonNullElse(amount, LootAmount.exactly(1));
    }
}
