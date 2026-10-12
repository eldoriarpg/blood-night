/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The drops of a custom mob; settings scoped {@code MOB} mean these, the others the world's or the
 * global ones. A mob killed by a player drops these on top of its vanilla loot.
 *
 * @param weightsScope whose {@code weights} decide how many drops fall
 * @param weights      weights of how many drops fall, by count: {@code 1: 90, 2: 50, 3: 10} drops one 60%, two 33%
 *                     and three 7% of the time
 * @param dropTables   whose drop lists every drop is picked from, together
 * @param dropList     this level's drop list
 * @param xpScope      whose {@code xpModifier} and {@code xpValue} change the experience dropped
 * @param xpModifier   how the experience changes
 * @param xpValue      the number it changes by
 */
public record MobDrops(
        ConfigurationScope weightsScope,
        Map<Integer, Integer> weights,
        List<ConfigurationScope> dropTables,
        List<Drop> dropList,
        ConfigurationScope xpScope,
        ValueModifier xpModifier,
        double xpValue) {
    public static MobDrops defaultDrops() {
        Map<Integer, Integer> weights = new HashMap<>() {
            {
                put(1, 90);
                put(2, 50);
                put(3, 10);
            }
        };
        ArrayList<ConfigurationScope> dropTables = new ArrayList<>() {
            {
                add(ConfigurationScope.GLOBAL);
                add(ConfigurationScope.WORLD);
                add(ConfigurationScope.MOB);
            }
        };
        return new MobDrops(
                ConfigurationScope.GLOBAL,
                weights,
                dropTables,
                new ArrayList<>(),
                ConfigurationScope.GLOBAL,
                ValueModifier.MULTIPLY,
                4);
    }
}
