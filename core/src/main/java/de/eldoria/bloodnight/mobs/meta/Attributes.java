package de.eldoria.bloodnight.mobs.meta;

public record Attributes(String displayName,
                         ValueModifier healthModifier, double health,
                         ValueModifier damageModifier, double damage) {
}
