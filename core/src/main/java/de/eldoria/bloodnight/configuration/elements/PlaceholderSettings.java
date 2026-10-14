/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

/**
 * How BloodNight's placeholders behave.
 */
public class PlaceholderSettings {
    private boolean revealNext = false;

    /**
     * @return whether {@code next_nights} and {@code next_time} tell when the next blood night comes; when not, they
     *     only tell about nights an admin forced
     */
    public boolean revealNext() {
        return revealNext;
    }
}
