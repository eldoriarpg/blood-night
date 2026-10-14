/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.mobsettings.AttributeOverride;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.CreeperSettings;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.MobSpawning;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * How mobs behave in a world during blood nights.
 * <p>
 * {@code damageModifier} scales the damage enemies deal to players, {@code healthMultiplier} makes ordinary enemies
 * that much harder to kill for players (their damage taken is divided by it). {@code forcePhantoms} makes phantoms
 * come for every player, rested or not. {@code displayNames} always shows the names of custom mobs above them; off by default, so a name only shows
 * when a player looks right at the mob.
 */
public class MobSettings {
    private MobSpawning spawning = new MobSpawning();
    private CreeperSettings creepers = new CreeperSettings();
    private boolean forcePhantoms = true;
    private boolean displayNames = false;
    private double damageModifier = 2;
    private double healthMultiplier = 2;
    private Map<String, AttributeOverride> overrides = new HashMap<>();

    public MobSpawning spawning() {
        return spawning;
    }

    public CreeperSettings creepers() {
        return creepers;
    }

    public boolean displayNames() {
        return displayNames;
    }

    public boolean forcePhantoms() {
        return forcePhantoms;
    }

    /**
     * @return the factor for damage enemies deal to players, at least 0
     */
    public double damageModifier() {
        return Math.max(damageModifier, 0);
    }

    /**
     * @return this world's own names, health and damage of custom mobs, by mob id
     */
    public Map<String, AttributeOverride> overrides() {
        return overrides;
    }

    /**
     * @return the mob's attributes in this world, its override put in
     */
    public Attributes attributes(CustomMob mob) {
        AttributeOverride override = overrides.get(mob.id());
        return override == null ? mob.attributes() : override.apply(mob.attributes());
    }

    /**
     * @return the divisor for damage players deal to ordinary enemies, above 0
     */
    public double healthMultiplier() {
        return healthMultiplier > 0 ? healthMultiplier : 1;
    }
}
