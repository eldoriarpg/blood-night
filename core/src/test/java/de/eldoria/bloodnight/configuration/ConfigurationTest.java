/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.night.LengthMode;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightSelectionType;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.defaults.DefaultMobs;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTest {
    private Plugin plugin;
    private Path mobs;

    @BeforeEach
    void setUp() throws IOException {
        MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        Files.createDirectories(plugin.getDataFolder().toPath());
        mobs = plugin.getDataFolder().toPath().resolve("mobs.yml");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private boolean backups() throws IOException {
        try (Stream<Path> files = Files.list(plugin.getDataFolder().toPath())) {
            return files.anyMatch(file -> file.getFileName().toString().startsWith("backup_"));
        }
    }

    @Test
    void unreadableUserDataIsReportedAndKept() throws IOException {
        Files.writeString(mobs, "mobs:\n  broken: [");
        Configuration configuration =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());

        assertTrue(configuration.mobs().all().isEmpty());
        assertTrue(configuration.problems().containsKey("mobs.yml"));
        assertEquals("mobs:\n  broken: [", Files.readString(mobs));
        assertTrue(!backups());
    }

    @Test
    void aBrokenFileDoesNotStopOthersFromReloading() throws IOException {
        Path items = plugin.getDataFolder().toPath().resolve("items.yml");
        Configuration configuration =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());
        Files.writeString(mobs, "mobs: {kept: {id: kept, extension: {type: NONE}}}");
        Files.writeString(items, "registrations: {}");
        assertEquals(1, configuration.mobs().all().size());
        configuration.items();

        Files.writeString(mobs, "mobs: {kept: [");
        Files.writeString(items, "registrations: {}\n");
        configuration.reload();

        assertTrue(configuration.mobs().all().isEmpty());
        assertTrue(configuration.problems().containsKey("mobs.yml"));
        assertTrue(!configuration.problems().containsKey("items.yml"));
        assertTrue(configuration.items().registrations().isEmpty());
    }

    @Test
    void reloadPicksUpTheFixedFileAndNeverResetsABrokenOne() throws IOException {
        Configuration configuration =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());
        configuration.mobs().add(Mobs.zombie("kept", Graph.builder().build()));
        Files.writeString(mobs, "mobs: {kept: {id: kept, extension: {type: NONE}}}");
        configuration.reload();
        assertEquals(1, configuration.mobs().all().size());

        Files.writeString(mobs, "mobs: {kept: [");
        configuration.reload();
        assertTrue(configuration.mobs().all().isEmpty());
        assertEquals("mobs: {kept: [", Files.readString(mobs));
        assertTrue(!backups());

        Files.writeString(mobs, "mobs: {kept: {id: kept, extension: {type: NONE}}}");
        configuration.reload();
        assertTrue(configuration.problems().isEmpty());
        assertEquals(1, configuration.mobs().all().size());
    }

    @Test
    void seedsTheDefaultMobsOnlyWhenTheFileIsMissing() throws IOException {
        GraphPlatform platform =
                BloodNightPlatform.create(CompileClasspath.discover(getClass().getClassLoader()));
        List<CustomMob> defaults = DefaultMobs.create(platform);
        new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader()).seedMobs(() -> defaults);

        Configuration reread =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());
        assertEquals(Map.of(), reread.problems());
        assertEquals(Set.copyOf(defaults), Set.copyOf(reread.mobs().all()));

        Files.writeString(mobs, "mobs: {}\n");
        new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader()).seedMobs(() -> defaults);
        assertEquals("mobs: {}\n", Files.readString(mobs));
    }

    @Test
    void worldSettingsAreReadFromTheirFile() throws IOException {
        World world = MockBukkit.getMock().addSimpleWorld("world");
        Path file = plugin.getDataFolder().toPath().resolve("worlds").resolve(world.getUID() + ".yml");
        Files.createDirectories(file.getParent());
        Files.writeString(file, """
                enabled: false
                nightSelection:
                  type: "CURVE"
                nightSettings:
                  begin: 12000
                  skippable: true
                  length:
                    mode: "EXTENDED"
                    seconds: 1000
                  beginCommands:
                  - "say begin"
                mobSettings:
                  spawning:
                    spawnPercentage: 7
                """);

        WorldSettings settings =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader()).worldConfig(world);

        assertFalse(settings.enabled());
        assertEquals(NightSelectionType.CURVE, settings.nightSelection().type());
        assertEquals(12000, settings.nightSettings().begin());
        assertTrue(settings.nightSettings().skippable());
        assertEquals(LengthMode.EXTENDED, settings.nightSettings().length().mode());
        assertEquals(List.of("say begin"), settings.nightSettings().beginCommands());
        assertEquals(7, settings.mobSettings().spawning().spawnPercentage());
    }
}
