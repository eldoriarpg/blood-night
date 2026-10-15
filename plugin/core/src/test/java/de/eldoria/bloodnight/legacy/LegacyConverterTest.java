/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.legacy;

import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.AttributeOverride;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightSelectionType;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.MobName;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.store.BuiltInStore;
import de.eldoria.bloodnight.store.MobStore;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyConverterTest {
    @TempDir
    Path folder;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void copy(String layout) throws IOException, URISyntaxException {
        Path source = Path.of(
                getClass().getClassLoader().getResource("legacy/" + layout).toURI());
        try (Stream<Path> files = Files.walk(source)) {
            for (Path file : files.toList()) {
                Path target = folder.resolve(source.relativize(file).toString());
                if (Files.isDirectory(file)) Files.createDirectories(target);
                else Files.copy(file, target);
            }
        }
    }

    private LegacyConverter.Result convert(Path legacy) throws IOException {
        LegacyConverter converter = new LegacyConverter(
                LegacyFolder.read(legacy),
                new MobStore(BuiltInStore.load()),
                LegacyNames::item,
                name -> Optional.of("minecraft:" + name.toLowerCase()));
        return converter.convert(
                new ConfigFile(),
                Map.of("world", new WorldSettings(), "world_nether", new WorldSettings()),
                List.of(),
                new ItemRegistry());
    }

    @Test
    void oldFilesAreMovedAsideOnce() throws Exception {
        copy("v2");

        Path moved = LegacyFolder.moveAside(folder).orElseThrow();

        assertEquals(folder.resolve("legacy"), moved);
        assertTrue(Files.exists(moved.resolve("worldSettings/world.yml")));
        assertFalse(Files.exists(folder.resolve("config.yml")));
        assertTrue(LegacyFolder.moveAside(folder).isEmpty());
    }

    @Test
    void worldsTakeTheOldSettings() throws Exception {
        copy("v2");
        LegacyConverter.Result result = convert(LegacyFolder.moveAside(folder).orElseThrow());

        WorldSettings world = result.worlds().get("world");
        assertEquals(NightSelectionType.CURVE, world.nightSelection().type());
        assertFalse(result.worlds().get("world_nether").enabled());
        assertEquals("<red>[BN]", result.main().announcements().prefix());
    }

    @Test
    void oldMobsComeFromTheStoreWithTheWorldsTuning() throws Exception {
        copy("v2");
        LegacyConverter.Result result = convert(LegacyFolder.moveAside(folder).orElseThrow());

        CustomMob armored = result.mobs().stream()
                .filter(mob -> mob.id().equals("armored_zombie"))
                .findFirst()
                .orElseThrow();
        assertTrue(armored.origin() != null);
        WorldSettings world = result.worlds().get("world");
        assertEquals(
                0,
                world.mobSettings()
                        .spawning()
                        .weight(result.mobs().stream()
                                .filter(mob -> mob.id().equals("speed_zombie"))
                                .findFirst()
                                .orElseThrow()));
        AttributeOverride override = world.mobSettings().overrides().get("armored_zombie");
        assertEquals(new MobName("<gold>", Map.of("en", "Tank")), override.name());
        assertEquals(ValueModifier.VALUE, override.healthModifier());
        assertEquals(40.0, override.health());

        String item = armored.mobDrops().dropList().getFirst().itemId();
        assertEquals(Material.DIAMOND, result.items().item(item).getType());
        assertEquals(5, armored.mobDrops().dropList().getFirst().weight());
    }

    @Test
    void theFirstLayoutKeepsWorldsInTheConfig() throws Exception {
        copy("v1");
        LegacyConverter.Result result = convert(folder);

        assertEquals(List.of("world"), List.copyOf(result.worlds().keySet()));
        assertTrue(result.notes().stream().anyMatch(note -> note.contains("mobTick")));
    }
}
