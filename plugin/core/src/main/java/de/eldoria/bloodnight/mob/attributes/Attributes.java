/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.attributes;

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
 * @param name           name shown above the mob, in each player's language; without texts it shows none
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
        MobName name,
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
        name = Objects.requireNonNullElse(name, MobName.NONE);
        changes = List.copyOf(Objects.requireNonNullElse(changes, List.of()));
        mythicType = Objects.requireNonNullElse(mythicType, "");
        mythicLevel = Math.max(mythicLevel, 1);
    }

    /**
     * Attributes of a vanilla based mob.
     */
    public Attributes(
            MobName name,
            EntityType entityType,
            boolean exactType,
            ValueModifier healthModifier,
            double health,
            ValueModifier damageModifier,
            double damage,
            List<AttributeChange> changes) {
        this(name, entityType, exactType, healthModifier, health, damageModifier, damage, changes, "", 1);
    }

    /**
     * @return the same attributes with another name
     */
    public Attributes withName(MobName name) {
        return new Attributes(
                name,
                entityType,
                exactType,
                healthModifier,
                health,
                damageModifier,
                damage,
                changes,
                mythicType,
                mythicLevel);
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
            MobName name,
            EntityType entityType,
            boolean exactType,
            ValueModifier healthModifier,
            double health,
            ValueModifier damageModifier,
            double damage) {
        this(name, entityType, exactType, healthModifier, health, damageModifier, damage, List.of());
    }

    public boolean isAssignable(EntityType type) {
        if (exactType) return type == entityType;
        Class<? extends Entity> thisClass = entityType.getEntityClass();
        Class<? extends Entity> thatClass = type.getEntityClass();
        return thisClass != null && thatClass != null && thisClass.isAssignableFrom(thatClass);
    }
}
