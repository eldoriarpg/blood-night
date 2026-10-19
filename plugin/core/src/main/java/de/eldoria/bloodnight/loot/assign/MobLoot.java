/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.assign;

import de.eldoria.bloodnight.loot.money.Money;
import de.eldoria.bloodnight.loot.roll.LootLevel;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The loot a special mob drops when a player kills it, on top of its vanilla loot.
 *
 * @param tables      the ids of the loot tables it assigns
 * @param slots       how many entries drop, as weights by count; null takes its world's, else the server's
 * @param inherit     whether the tables of its world and the server join the pool
 * @param replaceSame whether its items replace the same items of its world and the server
 * @param vanillaLoot what happens to its vanilla loot
 * @param experience  how its experience changes; null takes its world's, else the server's
 * @param money       what a kill pays; null takes its world's, else the server's
 */
public record MobLoot(
        List<String> tables,
        @Nullable Map<Integer, Integer> slots,
        Boolean inherit,
        Boolean replaceSame,
        VanillaLoot vanillaLoot,
        @Nullable Experience experience,
        @Nullable Money money) {
    public MobLoot {
        tables = List.copyOf(Objects.requireNonNullElse(tables, List.of()));
        slots = slots == null ? null : Map.copyOf(slots);
        inherit = Objects.requireNonNullElse(inherit, true);
        replaceSame = Objects.requireNonNullElse(replaceSame, true);
        vanillaLoot = Objects.requireNonNullElse(vanillaLoot, VanillaLoot.KEEP);
    }

    /**
     * @return loot of its own nothing: the world's and the server's tables decide
     */
    public static MobLoot inherited() {
        return new MobLoot(List.of(), null, true, true, VanillaLoot.KEEP, null, null);
    }

    /**
     * @return this mob's level of the roll
     */
    public LootLevel level() {
        return new LootLevel(tables, slots, replaceSame);
    }
}
