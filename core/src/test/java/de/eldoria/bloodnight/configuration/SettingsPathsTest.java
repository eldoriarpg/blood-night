/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.rules.WeatherChange;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsPathsTest {
    private final WorldSettings settings = new WorldSettings();

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pathsNameEverySetting() {
        var paths = SettingsPaths.paths(settings);

        assertTrue(paths.contains("nightSettings.skippable"));
        assertTrue(paths.contains("nightRules.weather"));
        assertTrue(paths.contains("nightRules.gameRules"));
        assertFalse(paths.contains("nightRules"));
    }

    @Test
    void valuesAreReadAndWritten() {
        assertEquals(
                "UNCHANGED",
                SettingsPaths.read(settings, "nightRules.weather").orElseThrow().asString());

        WorldSettings changed = SettingsPaths.write(settings, WorldSettings.class, "nightRules.weather", "THUNDER");
        changed = SettingsPaths.write(changed, WorldSettings.class, "nightSettings.skippable", "true");
        changed = SettingsPaths.write(
                changed, WorldSettings.class, "mobSettings.spawning.ignoredReasons", "[\"SPAWNER\"]");

        assertEquals(WeatherChange.THUNDER, changed.nightRules().weather());
        assertTrue(changed.nightSettings().skippable());
        assertEquals(
                Set.of(SpawnReason.SPAWNER), changed.mobSettings().spawning().ignoredReasons());
        assertEquals(WeatherChange.UNCHANGED, settings.nightRules().weather());
    }

    @Test
    void unknownPathsAndUnfittingValuesAreRefused() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SettingsPaths.write(settings, WorldSettings.class, "nightRules.storm", "true"));
        assertThrows(
                IllegalArgumentException.class,
                () -> SettingsPaths.write(settings, WorldSettings.class, "nightSettings.skippable", "maybe"));
        assertThrows(
                IllegalArgumentException.class,
                () -> SettingsPaths.write(settings, WorldSettings.class, "nightRules.weather", "HAIL"));
    }
}
