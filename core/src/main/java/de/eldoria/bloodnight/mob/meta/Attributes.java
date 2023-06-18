package de.eldoria.bloodnight.mob.meta;

import org.bukkit.entity.EntityType;

public record Attributes(String displayName,
                         EntityType entityType,
                         boolean exactType,
                         ValueModifier healthModifier, double health,
                         ValueModifier damageModifier, double damage) {
    public boolean isAssignable(EntityType type) {
        if(exactType) return type == entityType;
        return entityType.getEntityClass().isAssignableFrom(type.getEntityClass());
    }
}
