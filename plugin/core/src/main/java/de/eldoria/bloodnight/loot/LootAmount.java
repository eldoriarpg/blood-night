/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot;

import de.eldoria.bloodnight.loot.roll.Weighted;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.random.RandomGenerator;

/**
 * How many of an entry's item drop: any whole number from {@code min} to {@code max}, each as likely, or one of a few
 * amounts with a weight each, e.g. 10, 20 or 25.
 *
 * @param mode    which of the two applies
 * @param min     the least amount of a range, at least 1
 * @param max     the most of a range, at least {@code min}
 * @param choices amounts by their weight, for {@link AmountMode#CHOICES}; amounts below 1 and weights below 1 are left out
 */
public record LootAmount(AmountMode mode, int min, int max, Map<Integer, Integer> choices) {
    /** Whether an amount is a range or one of a few choices. */
    public enum AmountMode {
        /** Any whole number from {@code min} to {@code max}. */
        RANGE,
        /** One of {@code choices}, by weight. */
        CHOICES
    }

    public LootAmount {
        mode = Objects.requireNonNullElse(mode, AmountMode.RANGE);
        min = Math.max(1, min);
        max = Math.max(min, max);
        Map<Integer, Integer> kept = new TreeMap<>();
        Objects.requireNonNullElse(choices, Map.<Integer, Integer>of()).forEach((amount, weight) -> {
            if (amount != null && weight != null && amount >= 1 && weight >= 1) kept.put(amount, weight);
        });
        choices = Map.copyOf(kept);
    }

    /**
     * @param amount the one amount that drops
     * @return an amount that is always the same
     */
    public static LootAmount exactly(int amount) {
        return new LootAmount(AmountMode.RANGE, amount, amount, Map.of());
    }

    /**
     * @param random the source of chance
     * @return an amount to drop, at least 1
     */
    public int roll(RandomGenerator random) {
        if (mode == AmountMode.CHOICES && !choices.isEmpty()) {
            Integer picked = Weighted.pick(
                    choices.entrySet().stream()
                            .map(entry -> new Weighted<>(entry.getKey(), entry.getValue()))
                            .toList(),
                    random);
            return picked == null ? min : picked;
        }
        return min + random.nextInt(max - min + 1);
    }
}
