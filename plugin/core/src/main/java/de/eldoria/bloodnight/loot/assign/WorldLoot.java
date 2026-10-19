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
 * The loot of a world: what its special mobs roll beside their own tables, and what its ordinary enemies drop during
 * blood nights.
 *
 * @param tables      the ids of the loot tables special mobs here roll, unless a mob leaves them out
 * @param slots       how many entries drop, as weights by count; null takes the server's
 * @param inherit     whether the server's tables join the pool
 * @param replaceSame whether its items replace the same items of the server
 * @param experience  how special mobs' experience changes; null takes the server's
 * @param rewardLoot  whether a special mob a player kills during a blood night also drops a roll of the data pack's
 *                    loot table {@code bloodnight:blood_night/reward}
 * @param ordinary    what ordinary enemies drop
 * @param money       what a special mob's kill pays; null takes the server's
 */
public record WorldLoot(
        List<String> tables,
        @Nullable Map<Integer, Integer> slots,
        Boolean inherit,
        Boolean replaceSame,
        @Nullable Experience experience,
        Boolean rewardLoot,
        OrdinaryLoot ordinary,
        @Nullable Money money) {
    public WorldLoot {
        tables = List.copyOf(Objects.requireNonNullElse(tables, List.of()));
        slots = slots == null ? null : Map.copyOf(slots);
        inherit = Objects.requireNonNullElse(inherit, true);
        replaceSame = Objects.requireNonNullElse(replaceSame, false);
        rewardLoot = Objects.requireNonNullElse(rewardLoot, true);
        ordinary = Objects.requireNonNullElse(ordinary, OrdinaryLoot.defaults());
    }

    /**
     * @return the settings of a fresh world
     */
    public static WorldLoot defaults() {
        return new WorldLoot(List.of(), null, true, false, null, true, OrdinaryLoot.defaults(), null);
    }

    /**
     * @return this world's level of a special mob's roll
     */
    public LootLevel level() {
        return new LootLevel(tables, slots, replaceSame);
    }
}
