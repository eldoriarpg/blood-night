/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import org.bukkit.Statistic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhantomForceTest extends RulesTest {
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server.getPluginManager().registerEvents(new PhantomForce(nights, ignored -> true), plugin);
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        player.setStatistic(Statistic.TIME_SINCE_REST, 1_000);
    }

    @Test
    void phantomsComeDuringABloodNightOnly() {
        bloodNight();
        assertEquals(PhantomForce.RESTLESS, player.getStatistic(Statistic.TIME_SINCE_REST));
        day();
        assertEquals(1_000, player.getStatistic(Statistic.TIME_SINCE_REST));
    }

    @Test
    void leavingGivesTheTimeBack() {
        bloodNight();
        player.teleport(server.addSimpleWorld("other").getSpawnLocation());
        assertEquals(1_000, player.getStatistic(Statistic.TIME_SINCE_REST));
    }

    @Test
    void aMarkLeftByAStopIsRestored() {
        PhantomForce.mark(player);
        PhantomForce.mark(player);
        PhantomForce.restore(player);
        assertEquals(1_000, player.getStatistic(Statistic.TIME_SINCE_REST));
    }
}
