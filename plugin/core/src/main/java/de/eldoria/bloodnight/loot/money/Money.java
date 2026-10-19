/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.money;

import java.util.random.RandomGenerator;

/**
 * How much money a kill pays through Vault, rolled anew for every kill; equal values pay a fixed amount. The amount
 * is split among the players who damaged the mob.
 *
 * @param min the least a kill pays
 * @param max the most a kill pays
 */
public record Money(double min, double max) {
    public Money {
        min = Math.max(0, min);
        max = Math.max(min, max);
    }

    /**
     * @param random the source of chance
     * @return an amount from {@code min} to {@code max}, in cents
     */
    public double roll(RandomGenerator random) {
        double amount = min == max ? min : random.nextDouble(min, Math.nextUp(max));
        return Math.round(amount * 100) / 100.0;
    }
}
