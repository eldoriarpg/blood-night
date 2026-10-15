/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpawnCommandTest {
    private World world;

    @BeforeEach
    void setUp() {
        world = MockBukkit.mock().addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void mobsStandOnTheTopOfTheBlockLookedAt() {
        assertEquals(
                new Location(world, 3.5, 65, -1.5), SpawnCommand.besides(world.getBlockAt(3, 64, -2), BlockFace.UP));
    }

    @Test
    void mobsStandBesideAWallLookedAt() {
        assertEquals(
                new Location(world, 4.5, 64, -1.5), SpawnCommand.besides(world.getBlockAt(3, 64, -2), BlockFace.EAST));
    }
}
