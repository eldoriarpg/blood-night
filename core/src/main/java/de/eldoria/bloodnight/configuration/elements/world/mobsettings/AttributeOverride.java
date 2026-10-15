/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.mobsettings;

import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.MobName;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A world's own name, health or damage for one custom mob. What is left empty keeps the mob's value.
 *
 * @param name           the name shown above the mob; its texts win per language over the mob's, its style
 *                       replaces the mob's when set (see {@link MobName#over(MobName)})
 * @param healthModifier how {@code health} applies
 * @param health         the health value
 * @param damageModifier how {@code damage} applies
 * @param damage         the damage value
 */
public record AttributeOverride(
        @Nullable MobName name,
        @Nullable ValueModifier healthModifier,
        @Nullable Double health,
        @Nullable ValueModifier damageModifier,
        @Nullable Double damage) {
    /**
     * @return whether this world gives the mob a name of its own, which then has its own translation key
     */
    public boolean renames() {
        return name != null && !name.isEmpty();
    }

    /**
     * @return the mob's attributes with this world's values put in
     */
    public Attributes apply(Attributes attributes) {
        return new Attributes(
                name == null ? attributes.name() : name.over(attributes.name()),
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
