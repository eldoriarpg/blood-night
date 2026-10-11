/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightWatchTest {
    private final List<World> ended = new ArrayList<>();
    private final NightWatch watch = new NightWatch(ended::add);
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void nightIsWhenMonstersSpawnInTheOpen() {
        assertFalse(NightWatch.isNight(12_999));
        assertTrue(NightWatch.isNight(13_000));
        assertTrue(NightWatch.isNight(22_999));
        assertFalse(NightWatch.isNight(23_000));
        assertTrue(NightWatch.isNight(24_000 * 3 + 18_000));
    }

    @Test
    void endsTheNightOnceWhenTheWorldLeavesIt() {
        World world = server.addSimpleWorld("world");
        World other = server.addSimpleWorld("other");
        world.setTime(18_000);
        other.setTime(18_000);
        watch.check(List.of(world, other));

        world.setTime(23_500);
        watch.check(List.of(world, other));
        watch.check(List.of(world, other));

        assertEquals(List.of(world), ended);
    }

    @Test
    void aMorningWithoutANightEndsNothing() {
        World world = server.addSimpleWorld("world");
        world.setTime(1_000);
        watch.check(List.of(world));

        assertEquals(List.of(), ended);
    }
}
