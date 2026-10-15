/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * Rolls the loot of one kill from the levels that assign it, most specific first: the mob, its world, the server.
 * <ol>
 *     <li>The entries of every assigned table form one pool; a table assigned at more than one level counts once, at
 *     the most specific.</li>
 *     <li>A level that replaces the same items takes the entries with its items' ids out of every broader level.</li>
 *     <li>How many entries drop is rolled from the most specific level that sets it; without any, nothing drops.</li>
 *     <li>Each slot picks an entry by weight; an entry that is not repeatable leaves the pool once picked. Each picked
 *     entry rolls its own amount.</li>
 * </ol>
 */
public final class LootRoll {
    private LootRoll() {}

    /**
     * An entry in the pool of a kill and the table it comes from.
     *
     * @param table the table's id
     * @param entry the entry
     */
    public record Pooled(String table, LootEntry entry) {}

    /**
     * An entry picked for a kill.
     *
     * @param table  the id of the table it comes from
     * @param entry  the entry
     * @param amount how many of its item drop
     */
    public record Rolled(String table, LootEntry entry, int amount) {}

    /**
     * @param levels the levels that apply, most specific first
     * @param tables a table by id, or null for an unknown id, which is left out
     * @return every entry that can drop, after the more specific levels replaced theirs
     */
    public static List<Pooled> pool(List<LootLevel> levels, Function<String, @Nullable LootTable> tables) {
        Set<String> replaced = new HashSet<>();
        Set<String> used = new HashSet<>();
        List<Pooled> pool = new ArrayList<>();
        for (LootLevel level : levels) {
            List<Pooled> own = new ArrayList<>();
            for (String id : level.tables()) {
                LootTable table = used.add(id) ? tables.apply(id) : null;
                if (table == null) continue;
                table.entries().forEach(entry -> own.add(new Pooled(id, entry)));
            }
            own.stream()
                    .filter(pooled -> !replaced.contains(pooled.entry().item()))
                    .forEach(pool::add);
            if (level.replaceSame())
                own.forEach(pooled -> replaced.add(pooled.entry().item()));
        }
        return pool;
    }

    /**
     * @param levels the levels that apply, most specific first
     * @return how many entries drop, as weights by count, from the most specific level that sets it; empty for none
     */
    public static Map<Integer, Integer> slots(List<LootLevel> levels) {
        return levels.stream()
                .map(LootLevel::slots)
                .filter(slots -> slots != null)
                .findFirst()
                .orElse(Map.of());
    }

    /**
     * @param levels the levels that apply, most specific first
     * @param tables a table by id, or null for an unknown id
     * @param random the source of chance
     * @return what drops
     */
    public static List<Rolled> roll(
            List<LootLevel> levels, Function<String, @Nullable LootTable> tables, RandomGenerator random) {
        Integer count = Weighted.pick(
                slots(levels).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> new Weighted<>(entry.getKey(), entry.getValue()))
                        .toList(),
                random);
        if (count == null || count <= 0) return List.of();
        List<Pooled> pool = new ArrayList<>(pool(levels, tables));
        List<Rolled> rolled = new ArrayList<>();
        for (int slot = 0; slot < count; slot++) {
            Pooled picked = Weighted.pick(
                    pool.stream()
                            .map(pooled -> new Weighted<>(pooled, pooled.entry().weight()))
                            .toList(),
                    random);
            if (picked == null) break;
            if (!picked.entry().repeatable()) pool.remove(picked);
            rolled.add(new Rolled(
                    picked.table(), picked.entry(), picked.entry().amount().roll(random)));
        }
        return rolled;
    }
}
