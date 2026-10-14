/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.worlds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldNamesTest {
    private final Map<String, String> displayNames = new HashMap<>();
    private World world;
    private WorldNames names;

    @BeforeEach
    void setUp() {
        world = MockBukkit.mock().addSimpleWorld("world_nether");
        names = new WorldNames(
                target -> displayNames.getOrDefault(target.getName(), ""),
                () -> getClass().getClassLoader(),
                () -> null);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void withoutANameOrWorldPluginTheFolderNameShows() {
        assertEquals(Component.text("world_nether"), names.name(world));
    }

    @Test
    void theOwnDisplayNameWins() {
        displayNames.put("world_nether", "<red>The Nether");

        assertEquals(Component.text("The Nether", NamedTextColor.RED), names.name(world));
        assertEquals("The Nether", names.plain(world));
    }
}
