/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.announcement.BossBarSettings;
import de.eldoria.bloodnight.configuration.elements.world.announcement.NightEffect;

/**
 * How a world shows its blood nights to the players in it: a boss bar, an effect when the night begins and ends, a
 * warning when they enter or leave the world while a blood night runs, and a dialog explaining blood nights to each
 * player once, on their first one.
 */
public class AnnouncementSettings {
    private BossBarSettings bossBar = new BossBarSettings();
    private NightEffect effect = new NightEffect();
    private boolean joinWarning = true;
    private boolean introDialog = true;

    public BossBarSettings bossBar() {
        return bossBar;
    }

    public NightEffect effect() {
        return effect;
    }

    public boolean joinWarning() {
        return joinWarning;
    }

    /**
     * @return whether players see what blood nights are about on their first one
     */
    public boolean introDialog() {
        return introDialog;
    }
}
