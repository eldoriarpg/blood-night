/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.configuration.elements.Announcements;

/**
 * The server-wide settings in {@code config.yml}; everything about nights and mobs is set per world.
 */
public class ConfigFile {
    private Announcements announcements = new Announcements();

    /**
     * @return how blood nights are announced
     */
    public Announcements announcements() {
        return announcements;
    }
}
