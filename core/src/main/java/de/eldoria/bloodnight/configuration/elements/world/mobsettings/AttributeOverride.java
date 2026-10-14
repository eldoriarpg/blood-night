/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.mobsettings;

import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A world's own name, health or damage for one custom mob. What is left empty keeps the mob's value.
 *
 * @param displayName    the name shown above the mob, MiniMessage
 * @param healthModifier how {@code health} applies
 * @param health         the health value
 * @param damageModifier how {@code damage} applies
 * @param damage         the damage value
 */
public record AttributeOverride(
        @Nullable String displayName,
        @Nullable ValueModifier healthModifier,
        @Nullable Double health,
        @Nullable ValueModifier damageModifier,
        @Nullable Double damage) {
    /**
     * @return the mob's attributes with this world's values put in
     */
    public Attributes apply(Attributes attributes) {
        return new Attributes(
                displayName == null || displayName.isEmpty() ? attributes.displayName() : displayName,
                attributes.entityType(),
                attributes.exactType(),
                Objects.requireNonNullElse(healthModifier, attributes.healthModifier()),
                Objects.requireNonNullElse(health, attributes.health()),
                Objects.requireNonNullElse(damageModifier, attributes.damageModifier()),
                Objects.requireNonNullElse(damage, attributes.damage()),
                attributes.changes(),
                attributes.mythicType(),
                attributes.mythicLevel());
    }
}
