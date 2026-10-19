/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.configuration.world.night.LengthMode;
import de.eldoria.bloodnight.configuration.world.nightselection.NightSelectionType;
import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.store.MobStore;
import de.eldoria.bloodnight.store.StoreState;
import de.eldoria.bloodnight.store.build.BuiltInStore;
import de.eldoria.graph.model.Graph;
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
    void theEditorUrlCanBeOverridden() {
        System.setProperty("bloodnight.editor.url", "http://localhost:8080");
        try {
            Configuration configuration = new Configuration(
                    plugin.getDataFolder().toPath(), getClass().getClassLoader());
            assertEquals("http://localhost:8080", configuration.main().editor().url());
        } finally {
            System.clearProperty("bloodnight.editor.url");
        }
    }

    @Test
    void installedMobsKeepTheirLogicThroughTheFile() {
        MobStore store = new MobStore(BuiltInStore.load());
        List<CustomMob> installed =
                store.install(List.of(), Set.of(), BuiltInStore.CLASSIC).mobs();
        new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader()).replaceMobs(installed);

        Configuration reread =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());
        assertEquals(Map.of(), reread.problems());
        assertEquals(Set.copyOf(installed), Set.copyOf(reread.mobs().all()));
        reread.mobs().all().forEach(mob -> assertEquals(StoreState.CURRENT, store.state(mob), mob.id()));
    }

    @Test
    void hashesFollowTheFilesOnDisk() throws IOException {
        World world = MockBukkit.getMock().addSimpleWorld("world");
        Configuration configuration =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());
        configuration.worldConfig(world);
        configuration.main();
        Map<String, String> opened = configuration.fileHashes(List.of(world));
        String worldFile = "worlds/" + world.getUID() + ".yml";

        assertEquals(Configuration.MISSING, opened.get("macros.yml"));
        assertEquals(opened, configuration.fileHashes(List.of(world)));
        Path file = plugin.getDataFolder().toPath().resolve(worldFile);
        Files.writeString(file, Files.readString(file) + "\n");
        Map<String, String> now = configuration.fileHashes(List.of(world));
        assertEquals(opened.get("config.yml"), now.get("config.yml"));
        assertFalse(opened.get(worldFile).equals(now.get(worldFile)));
    }

    @Test
    void editedFilesReplaceTheOldOnesAfterABackup() throws IOException {
        World world = MockBukkit.getMock().addSimpleWorld("world");
        Configuration configuration =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());
        configuration.worldConfig(world);
        configuration.replaceMobs(List.of(Mobs.zombie("edited", Graph.builder().build())));

        Path backup = configuration.backup();
        configuration.replaceWorld(world, EditorPayload.MAPPER.readValue("{\"enabled\":false}", WorldSettings.class));
        configuration.replaceMobs(List.of());

        assertTrue(Files.exists(backup.resolve("mobs.yml")));
        assertTrue(Files.readString(backup.resolve("mobs.yml")).contains("edited"));
        assertTrue(Files.exists(backup.resolve("worlds").resolve(world.getUID() + ".yml")));
        Configuration reread =
                new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader());
        assertFalse(reread.worldConfig(world).enabled());
        assertTrue(reread.mobs().all().isEmpty());
    }

    @Test
    void settingsFilesGainMissingKeysAndKeepTheirValues() throws IOException {
        World world = MockBukkit.getMock().addSimpleWorld("world");
        Path file = plugin.getDataFolder().toPath().resolve("worlds").resolve(world.getUID() + ".yml");
        Files.createDirectories(file.getParent());
        Files.writeString(file, """
                enabled: false
                soundSettings: {}
                """);

        new Configuration(plugin.getDataFolder().toPath(), getClass().getClassLoader()).worldConfig(world);

        String written = Files.readString(file);
        assertTrue(written.contains("enabled: false"), written);
        assertTrue(written.contains("announcement:"), written);
        assertTrue(written.contains("minInterval:"), written);
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
