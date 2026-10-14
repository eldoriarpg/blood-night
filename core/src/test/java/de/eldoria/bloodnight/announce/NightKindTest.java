/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NightKindTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static World world(World.Environment environment) {
        WorldMock world = new WorldMock();
        world.setEnvironment(environment);
        return world;
    }

    @Test
    void eachWorldNamesItsBloodNight() {
        assertEquals(
                "bloodnight.night.name",
                NightKind.of(world(World.Environment.NORMAL)).key("name"));
        assertEquals(
                "bloodnight.tide.name",
                NightKind.of(world(World.Environment.NETHER)).key("name"));
        assertEquals(
                "bloodnight.eclipse.name",
                NightKind.of(world(World.Environment.THE_END)).key("name"));
    }
}
