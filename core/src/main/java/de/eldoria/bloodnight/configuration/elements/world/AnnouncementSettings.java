/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.announcement.BossBarSettings;
import de.eldoria.bloodnight.configuration.elements.world.announcement.NightEffect;

/**
 * How a world shows its blood nights to the players in it: a boss bar, an effect when the night begins and ends, and
 * a warning when they enter or leave the world while a blood night runs.
 */
public class AnnouncementSettings {
    private BossBarSettings bossBar = new BossBarSettings();
    private NightEffect effect = new NightEffect();
    private boolean joinWarning = true;

    public BossBarSettings bossBar() {
        return bossBar;
    }

    public NightEffect effect() {
        return effect;
    }

    public boolean joinWarning() {
        return joinWarning;
    }
}
