/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.ValueModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The global drops, used where world or mob drop settings are scoped {@code GLOBAL}.
 *
 * @param weights    weights of how many drops fall, by count
 * @param drops      the global drop list
 * @param xpModifier how the experience a mob drops changes
 * @param xpValue    the number it changes by
 */
public record GlobalDrops(Map<Integer, Integer> weights, List<Drop> drops, ValueModifier xpModifier, double xpValue) {
    public static GlobalDrops defaultDrops() {
        Map<Integer, Integer> weights = new HashMap<>() {
            {
                put(1, 90);
                put(2, 50);
                put(3, 10);
            }
        };
        return new GlobalDrops(weights, new ArrayList<>(), ValueModifier.MULTIPLY, 4);
    }
}
