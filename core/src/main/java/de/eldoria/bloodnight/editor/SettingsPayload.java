/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;

import java.util.Map;

/**
 * The settings the editor edits: the server-wide settings, the global drops and every world's settings.
 *
 * @param main        {@code config.yml}
 * @param globalDrops {@code global_drops.yml}
 * @param worlds      each world's settings by the world's UID
 */
public record SettingsPayload(ConfigFile main, GlobalDrops globalDrops, Map<String, WorldPayload> worlds) {
    /**
     * @param name     the world's name, for display
     * @param settings its settings
     */
    public record WorldPayload(String name, WorldSettings settings) {}
}
