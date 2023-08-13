package de.eldoria.bloodnight.mob.meta;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public record Attributes(String displayName,
                         EntityType entityType,
                         boolean exactType,
                         ValueModifier healthModifier, double health,
                         ValueModifier damageModifier, double damage) {
    public boolean isAssignable(EntityType type) {
        if(exactType) return type == entityType;
        Class<? extends Entity> thisClass = entityType.getEntityClass();
        Class<? extends Entity> thatClass = type.getEntityClass();
        return thisClass != null && thatClass != null && thisClass.isAssignableFrom(thatClass);
    }
}
