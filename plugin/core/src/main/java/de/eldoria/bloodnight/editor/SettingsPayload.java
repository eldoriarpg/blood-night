/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import org.bukkit.World;

import java.util.Map;

/**
 * The settings the editor edits: the server-wide settings and every world's settings.
 *
 * @param main   {@code config.yml}
 * @param worlds each world's settings by the world's UID
 */
public record SettingsPayload(ConfigFile main, Map<String, WorldPayload> worlds) {
    /**
     * @param name        the world's name, for display
     * @param environment its kind, {@code NORMAL}, {@code NETHER}, {@code THE_END} or {@code CUSTOM}; the editor offers
     *                    only the settings that matter in it, e.g. the dragon fight in the End
     * @param dayCycle    whether its time goes on, which decides between night times and timed blood nights
     * @param settings    its settings
     * @param alias       its name in Multiverse-Core or Hyperverse, empty when it has none; players see it unless the
     *                    settings give the world a {@code displayName}
     */
    public record WorldPayload(
            String name, String environment, boolean dayCycle, WorldSettings settings, String alias) {
        /**
         * @param alias the world's name in a world plugin, empty for none
         * @return the world's entry
         */
        public static WorldPayload of(World world, WorldSettings settings, String alias) {
            return new WorldPayload(
                    world.getName(), world.getEnvironment().name(), !world.isFixedTime(), settings, alias);
        }
    }
}
