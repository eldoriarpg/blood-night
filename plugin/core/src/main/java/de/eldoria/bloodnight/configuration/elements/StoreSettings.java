/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

/**
 * How mobs from the store are kept up to date.
 */
public class StoreSettings {
    private boolean autoUpdate = true;

    /**
     * @return whether store mobs nobody changed take the store's newer versions on start
     */
    public boolean autoUpdate() {
        return autoUpdate;
    }
}
