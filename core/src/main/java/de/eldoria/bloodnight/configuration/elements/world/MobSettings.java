/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.mobsettings.CreeperSettings;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.MobSpawning;

/**
 * How mobs behave in a world during blood nights.
 * <p>
 * {@code damageModifier} scales the damage enemies deal to players, {@code healthMultiplier} makes ordinary enemies
 * that much harder to kill for players (their damage taken is divided by it). {@code forcePhantoms} makes phantoms
 * come for every player, rested or not. {@code displayNames} shows the names of custom mobs above them.
 */
public class MobSettings {
    private MobSpawning spawning = new MobSpawning();
    private CreeperSettings creepers = new CreeperSettings();
    private boolean forcePhantoms = true;
    private boolean displayNames = true;
    private double damageModifier = 2;
    private double healthMultiplier = 2;

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
     * @return the divisor for damage players deal to ordinary enemies, above 0
     */
    public double healthMultiplier() {
        return healthMultiplier > 0 ? healthMultiplier : 1;
    }
}
