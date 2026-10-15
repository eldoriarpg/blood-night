/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.ValueModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The drops of custom mobs in a world that set no drops of their own; settings scoped {@code WORLD}
 * mean these, {@code GLOBAL} the global ones. A mob killed by a player drops these on top of its vanilla loot.
 *
 * @param weightsScope whose {@code weights} decide how many drops fall
 * @param weights      weights of how many drops fall, by count: {@code 1: 90, 2: 50, 3: 10} drops one 60%, two 33%
 *                     and three 7% of the time
 * @param dropTables   whose drop lists every drop is picked from, together
 * @param dropList     this level's drop list
 * @param xpScope      whose {@code xpModifier} and {@code xpValue} change the experience dropped
 * @param xpModifier   how the experience changes
 * @param xpValue      the number it changes by
 * @param vanilla      what ordinary enemies a player kills during a blood night drop
 * @param rewardLoot   whether a custom mob a player kills during a blood night also drops a roll of the loot table
 *                     {@code bloodnight:blood_night/reward}, which a data pack can replace
 */
public record WorldDrops(
        ConfigurationScope weightsScope,
        Map<Integer, Integer> weights,
        List<ConfigurationScope> dropTables,
        List<Drop> dropList,
        ConfigurationScope xpScope,
        ValueModifier xpModifier,
        double xpValue,
        VanillaDrops vanilla,
        Boolean rewardLoot) {
    /**
     * Fills in the vanilla drop settings and the reward when a file has none.
     */
    public WorldDrops {
        if (vanilla == null) vanilla = VanillaDrops.defaultDrops();
        if (rewardLoot == null) rewardLoot = true;
    }

    public static WorldDrops defaultDrops() {
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
            }
        };
        return new WorldDrops(
                ConfigurationScope.GLOBAL,
                weights,
                dropTables,
                new ArrayList<>(),
                ConfigurationScope.GLOBAL,
                ValueModifier.MULTIPLY,
                4,
                VanillaDrops.defaultDrops(),
                true);
    }
}
