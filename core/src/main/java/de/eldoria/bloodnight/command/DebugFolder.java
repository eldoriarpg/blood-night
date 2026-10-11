/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The {@code debug} folder in the plugin's data folder, where commands write files for inspection.
 */
final class DebugFolder {
    private DebugFolder() {}

    /**
     * @param plugin the plugin
     * @return the folder, created when missing
     * @throws IOException when it cannot be created
     */
    static Path of(Plugin plugin) throws IOException {
        Path directory = plugin.getDataFolder().toPath().resolve("debug");
        Files.createDirectories(directory);
        return directory;
    }
}
