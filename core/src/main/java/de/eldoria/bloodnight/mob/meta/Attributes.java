/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Objects;

/**
 * What a custom mob is and how strong it is.
 * <p>
 * {@code DEFAULT} health is the entity's own times the world's health multiplier, the same toughness ordinary enemies
 * get during blood nights; {@code DEFAULT} damage is the entity's own, the world's damage modifier applies to every
 * enemy anyway. {@code MULTIPLY} multiplies the entity's own value, {@code VALUE} replaces it.
 *
 * @param displayName    name shown above the mob, MiniMessage; empty shows none
 * @param entityType     the type of entity the mob takes over
 * @param exactType      whether only that type matches, not its subtypes (a husk is a zombie)
 * @param healthModifier how {@code health} applies
 * @param health         the health value
 * @param damageModifier how {@code damage} applies
 * @param damage         the damage value
 * @param changes        further changes to any attribute
 * @param mythicType     a MythicMobs mob that spawns in place of the entity, empty for none; health, damage,
 *                       equipment and extension are then MythicMobs' business
 * @param mythicLevel    the level of that MythicMobs mob, at least 1
 */
public record Attributes(
        String displayName,
        EntityType entityType,
        boolean exactType,
        ValueModifier healthModifier,
        double health,
        ValueModifier damageModifier,
        double damage,
        List<AttributeChange> changes,
        String mythicType,
        int mythicLevel) {
    public Attributes {
        changes = List.copyOf(Objects.requireNonNullElse(changes, List.of()));
        mythicType = Objects.requireNonNullElse(mythicType, "");
        mythicLevel = Math.max(mythicLevel, 1);
    }

    /**
     * Attributes of a vanilla based mob.
     */
    public Attributes(
            String displayName,
            EntityType entityType,
            boolean exactType,
            ValueModifier healthModifier,
            double health,
            ValueModifier damageModifier,
            double damage,
            List<AttributeChange> changes) {
        this(displayName, entityType, exactType, healthModifier, health, damageModifier, damage, changes, "", 1);
    }

    /**
     * @return whether a MythicMobs mob spawns in place of the entity
     */
    public boolean mythic() {
        return !mythicType.isBlank();
    }

    /**
     * Attributes without further changes.
     */
    public Attributes(
            String displayName,
            EntityType entityType,
            boolean exactType,
            ValueModifier healthModifier,
            double health,
            ValueModifier damageModifier,
            double damage) {
        this(displayName, entityType, exactType, healthModifier, health, damageModifier, damage, List.of());
    }

    public boolean isAssignable(EntityType type) {
        if (exactType) return type == entityType;
        Class<? extends Entity> thisClass = entityType.getEntityClass();
        Class<? extends Entity> thatClass = type.getEntityClass();
        return thisClass != null && thatClass != null && thisClass.isAssignableFrom(thatClass);
    }
}
