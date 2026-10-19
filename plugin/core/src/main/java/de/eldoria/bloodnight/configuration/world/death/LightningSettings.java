/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.death;

/**
 * A harmless lightning strike above a death, or else a thunder sound for the world, each with a chance in percent.
 */
public class LightningSettings {
    private boolean lightning = true;
    private int lightningChance = 100;
    private boolean thunder = true;
    private int thunderChance = 100;

    public boolean lightning() {
        return lightning;
    }

    /**
     * @return the chance of a lightning strike, 0 to 100
     */
    public int lightningChance() {
        return Math.clamp(lightningChance, 0, 100);
    }

    public boolean thunder() {
        return thunder;
    }

    /**
     * @return the chance of a thunder sound when no lightning struck, 0 to 100
     */
    public int thunderChance() {
        return Math.clamp(thunderChance, 0, 100);
    }
}
