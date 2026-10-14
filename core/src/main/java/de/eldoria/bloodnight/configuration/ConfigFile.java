/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.configuration.elements.Announcements;
import de.eldoria.bloodnight.configuration.elements.EditorSettings;
import de.eldoria.bloodnight.configuration.elements.ForeignMobSettings;
import de.eldoria.bloodnight.configuration.elements.PlaceholderSettings;
import de.eldoria.bloodnight.configuration.elements.ResourcePackSettings;
import de.eldoria.bloodnight.configuration.elements.UpdateSettings;

/**
 * The server-wide settings in {@code config.yml}; everything about nights and mobs is set per world.
 */
public class ConfigFile {
    private Announcements announcements = new Announcements();
    private EditorSettings editor = new EditorSettings();
    private ResourcePackSettings resourcePack = new ResourcePackSettings();
    private PlaceholderSettings placeholders = new PlaceholderSettings();
    private UpdateSettings updates = new UpdateSettings();
    private ForeignMobSettings foreignMobs = new ForeignMobSettings();

    /**
     * @return how blood nights are announced
     */
    public Announcements announcements() {
        return announcements;
    }

    /**
     * @return where the web editor's service runs
     */
    public EditorSettings editor() {
        return editor;
    }

    /**
     * @return the resource pack offered to players
     */
    public ResourcePackSettings resourcePack() {
        return resourcePack;
    }

    /**
     * @return whether the server hears about new versions
     */
    public UpdateSettings updates() {
        return updates;
    }

    /**
     * @return how the placeholders behave
     */
    public PlaceholderSettings placeholders() {
        return placeholders;
    }

    /**
     * @return how mobs of other plugins are treated
     */
    public ForeignMobSettings foreignMobs() {
        return foreignMobs;
    }
}
