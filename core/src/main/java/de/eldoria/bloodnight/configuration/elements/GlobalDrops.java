package de.eldoria.bloodnight.configuration.elements;

import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.ValueModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GlobalDrops(Map<Integer, Integer> weights, List<Drop> drops, ValueModifier xpModifier, double xpValue) {
    public static GlobalDrops defaultDrops() {
        Map<Integer, Integer> weights = new HashMap<>() {{
            put(1, 90);
            put(2, 50);
            put(3, 10);
        }};
        return new GlobalDrops(weights, new ArrayList<>(), ValueModifier.MULTIPLY, 4);
    }
}
