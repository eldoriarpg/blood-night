/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.mobsettings;

/**
 * Whether creepers, special ones included, break blocks. Protection applies during blood nights, or at all times with
 * {@code always}; the explosion still hurts entities.
 */
public class CreeperSettings {
    private boolean blockDamage;
    private boolean always;

    public boolean blockDamage() {
        return blockDamage;
    }

    public boolean always() {
        return always;
    }
}
