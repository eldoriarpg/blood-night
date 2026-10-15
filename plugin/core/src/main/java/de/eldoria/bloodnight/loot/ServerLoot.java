/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import de.eldoria.bloodnight.mob.meta.ValueModifier;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The loot of the whole server: the broadest level of every roll.
 *
 * @param tables     the ids of the loot tables special mobs everywhere roll, unless their world or they leave them out
 * @param slots      how many entries drop, as weights by count, where no world or mob sets it
 * @param ordinary   what ordinary enemies roll on top of their world's tables during blood nights
 * @param experience how special mobs' experience changes where no world or mob sets it
 */
public record ServerLoot(
        List<String> tables, @Nullable Map<Integer, Integer> slots, Ordinary ordinary, Experience experience) {
    /**
     * The server's loot for ordinary enemies.
     *
     * @param tables the ids of the loot tables they roll in every world that keeps them
     * @param slots  how many entries drop, where a world does not set it
     */
    public record Ordinary(List<String> tables, @Nullable Map<Integer, Integer> slots) {
        public Ordinary {
            tables = List.copyOf(Objects.requireNonNullElse(tables, List.of()));
            slots = slots == null ? null : Map.copyOf(slots);
        }

        /**
         * @return the server's level of an ordinary enemy's roll
         */
        public LootLevel level() {
            return new LootLevel(tables, slots, false);
        }
    }

    public ServerLoot {
        tables = List.copyOf(Objects.requireNonNullElse(tables, List.of()));
        slots = slots == null ? Map.of(1, 90, 2, 50, 3, 10) : Map.copyOf(slots);
        ordinary = Objects.requireNonNullElse(ordinary, new Ordinary(List.of(), Map.of(1, 1)));
        experience = Objects.requireNonNullElse(experience, new Experience(ValueModifier.MULTIPLY, 4));
    }

    /**
     * @return the settings of a fresh server
     */
    public static ServerLoot defaults() {
        return new ServerLoot(
                List.of(),
                Map.of(1, 90, 2, 50, 3, 10),
                new Ordinary(List.of(), Map.of(1, 1)),
                new Experience(ValueModifier.MULTIPLY, 4));
    }

    /**
     * @return the server's level of a special mob's roll
     */
    public LootLevel level() {
        return new LootLevel(tables, slots, false);
    }
}
