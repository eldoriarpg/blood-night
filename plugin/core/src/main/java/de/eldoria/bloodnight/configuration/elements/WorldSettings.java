/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

import de.eldoria.bloodnight.configuration.elements.world.AnnouncementSettings;
import de.eldoria.bloodnight.configuration.elements.world.AtmosphereSettings;
import de.eldoria.bloodnight.configuration.elements.world.MobSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightRuleSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.configuration.elements.world.NightSettings;
import de.eldoria.bloodnight.configuration.elements.world.SoundSettings;
import de.eldoria.bloodnight.configuration.elements.world.death.DeathActionSettings;
import de.eldoria.bloodnight.loot.WorldLoot;

/**
 * The settings of one world. Fields are not final: the configuration reads them in place, and Jackson 3 does not write
 * final fields.
 */
public class WorldSettings {
    private boolean enabled = false;
    private String displayName = "";
    private NightSelection nightSelection = new NightSelection();
    private NightSettings nightSettings = new NightSettings();
    private MobSettings mobSettings = new MobSettings();
    private AnnouncementSettings announcement = new AnnouncementSettings();
    private AtmosphereSettings atmosphere = new AtmosphereSettings();
    private SoundSettings soundSettings = new SoundSettings();
    private DeathActionSettings deathActions = new DeathActionSettings();
    private WorldLoot loot = WorldLoot.defaults();
    private NightRuleSettings nightRules = new NightRuleSettings();

    /**
     * @return whether the world has blood nights at all; off until the setup or an admin turns it on
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * @return the world's name for players, MiniMessage; empty uses the alias of a world plugin or the world's name
     */
    public String displayName() {
        return displayName;
    }

    public NightSelection nightSelection() {
        return nightSelection;
    }

    public NightSettings nightSettings() {
        return nightSettings;
    }

    public MobSettings mobSettings() {
        return mobSettings;
    }

    /**
     * @return how the world itself changes during a blood night
     */
    public NightRuleSettings nightRules() {
        return nightRules;
    }

    /**
     * @return what happens when players and enemies die during a blood night
     */
    public DeathActionSettings deathActions() {
        return deathActions;
    }

    /**
     * @return how blood nights are shown to the players
     */
    public AnnouncementSettings announcement() {
        return announcement;
    }

    /**
     * @return how blood nights look and feel to the players
     */
    public AtmosphereSettings atmosphere() {
        return atmosphere;
    }

    public SoundSettings soundSettings() {
        return soundSettings;
    }

    public WorldLoot loot() {
        return loot;
    }
}
