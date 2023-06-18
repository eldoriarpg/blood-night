package de.eldoria.bloodnight.mob.meta;

import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the drops of a mob
 *
 * @param weights
 * @param weightsScope
 * @param dropTables
 * @param dropList
 * @param xpModifier
 * @param xpValue
 */
public record MobDrops(
        ConfigurationScope weightsScope, Map<Integer, Integer> weights,
        List<ConfigurationScope> dropTables, List<Drop> dropList,
        ConfigurationScope xpScope, ValueModifier xpModifier, double xpValue) {
    public static MobDrops defaultDrops() {
        Map<Integer, Integer> weights = new HashMap<>() {{
            put(1, 90);
            put(2, 50);
            put(3, 10);
        }};
        ArrayList<ConfigurationScope> dropTables = new ArrayList<>() {{
            add(ConfigurationScope.GLOBAL);
            add(ConfigurationScope.WORLD);
            add(ConfigurationScope.MOB);
        }};
        return new MobDrops(ConfigurationScope.GLOBAL, weights, dropTables, new ArrayList<>(), ConfigurationScope.GLOBAL, ValueModifier.MULTIPLY, 4);
    }
}
