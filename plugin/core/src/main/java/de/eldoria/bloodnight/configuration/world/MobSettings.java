/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world;

import de.eldoria.bloodnight.configuration.world.mobsettings.AttributeOverride;
import de.eldoria.bloodnight.configuration.world.mobsettings.CreeperSettings;
import de.eldoria.bloodnight.configuration.world.mobsettings.MobSpawning;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.attributes.Attributes;
import org.bukkit.World;

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
    private Map<String, Integer> withers =
            new HashMap<>(Map.of("blink_wither", 1, "blood_wither", 1, "grave_wither", 1));

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
     * @return the mobs a wither built during a blood night becomes, by id, with their weights; empty keeps withers
     *     vanilla
     */
    public Map<String, Integer> withers() {
        return withers;
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
     * @param mob   a custom mob
     * @param world the world it is in
     * @return the key of the mob's name in this world, below {@code bloodnight.mob.}: its id, or its id and the world's
     *     UID where this world gives it a name of its own
     */
    public String nameKey(CustomMob mob, World world) {
        AttributeOverride override = overrides.get(mob.id());
        return override != null && override.renames() ? mob.id() + "." + world.getUID() : mob.id();
    }

    /**
     * @return the divisor for damage players deal to ordinary enemies, above 0
     */
    public double healthMultiplier() {
        return healthMultiplier > 0 ? healthMultiplier : 1;
    }
}
