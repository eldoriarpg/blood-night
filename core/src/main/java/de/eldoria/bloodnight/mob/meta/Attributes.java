package de.eldoria.bloodnight.mob.meta;

public record Attributes(String displayName,
                         ValueModifier healthModifier, double health,
                         ValueModifier damageModifier, double damage) {
}
