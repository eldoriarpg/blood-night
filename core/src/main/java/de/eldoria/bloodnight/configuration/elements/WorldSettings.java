/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

import de.eldoria.bloodnight.configuration.elements.world.AnnouncementSettings;
import de.eldoria.bloodnight.configuration.elements.world.MobSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.configuration.elements.world.NightSettings;
import de.eldoria.bloodnight.configuration.elements.world.SoundSettings;
import de.eldoria.bloodnight.configuration.elements.world.WorldDrops;
import de.eldoria.bloodnight.configuration.elements.world.death.DeathActionSettings;

/**
 * The settings of one world. Fields are not final: the configuration reads them in place, and Jackson 3 does not write
 * final fields.
 */
public class WorldSettings {
    private boolean enabled = true;
    private NightSelection nightSelection = new NightSelection();
    private NightSettings nightSettings = new NightSettings();
    private MobSettings mobSettings = new MobSettings();
    private AnnouncementSettings announcement = new AnnouncementSettings();
    private SoundSettings soundSettings = new SoundSettings();
    private DeathActionSettings deathActions = new DeathActionSettings();
    private WorldDrops worldDrops = WorldDrops.defaultDrops();

    /**
     * @return whether the world has blood nights at all
     */
    public boolean enabled() {
        return enabled;
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

    public SoundSettings soundSettings() {
        return soundSettings;
    }

    public WorldDrops worldDrops() {
        return worldDrops;
    }
}
