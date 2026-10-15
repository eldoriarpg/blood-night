/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.debug;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DebugReportTest {
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("BloodNight");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void carriesTheSettingsButNeitherMobsNorTheSessionToken() throws IOException {
        Path folder = plugin.getDataFolder().toPath();
        Files.createDirectories(folder.resolve("worlds"));
        for (String file : List.of(
                "config.yml",
                "global_drops.yml",
                "mobs.yml",
                "items.yml",
                "macros.yml",
                "editor-session.json",
                "worlds/b.yml",
                "worlds/a.yml")) {
            Files.writeString(folder.resolve(file), "x: 1");
        }

        List<String> carried = DebugReport.settings().configFiles(plugin).stream()
                .map(path -> folder.relativize(path).toString().replace('\\', '/'))
                .toList();

        assertEquals(List.of("config.yml", "global_drops.yml", "worlds/a.yml", "worlds/b.yml"), carried);
    }
}
