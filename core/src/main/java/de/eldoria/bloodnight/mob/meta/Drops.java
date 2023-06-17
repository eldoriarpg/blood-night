package de.eldoria.bloodnight.mobs.meta;

import java.util.List;

public record Drops(int dropAmount, boolean overrideDefaultDrops, List<Drop> dropList,
                    ValueModifier xpModifier, double xpValue) {
}
