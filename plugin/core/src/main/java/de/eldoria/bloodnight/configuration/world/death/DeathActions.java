/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.death;

/**
 * What happens where something dies during a blood night.
 */
public class DeathActions {
    private LightningSettings lightning = new LightningSettings();
    private ShockwaveSettings shockwave = new ShockwaveSettings();

    public LightningSettings lightning() {
        return lightning;
    }

    public ShockwaveSettings shockwave() {
        return shockwave;
    }
}
