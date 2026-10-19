/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce.night;

import de.eldoria.bloodnight.configuration.world.announcement.BossBarSettings;
import de.eldoria.bloodnight.night.selection.NightTimes;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightBarsTest {
    @Test
    void barEmptiesOverTheNight() {
        NightTimes times = new NightTimes(13_000, 23_000);
        assertEquals(1f, NightBars.remaining(times, 2, 2 * 24_000 + 13_000));
        assertEquals(0.5f, NightBars.remaining(times, 2, 2 * 24_000 + 18_000));
        assertEquals(0f, NightBars.remaining(times, 2, 2 * 24_000 + 23_000));
        assertEquals(1f, NightBars.remaining(times, 2, 2 * 24_000 + 12_000));
    }

    @Test
    void barFollowsANightPastMidnight() {
        NightTimes times = new NightTimes(20_000, 4_000);
        assertEquals(0.5f, NightBars.remaining(times, 0, 24_000));
    }

    @Test
    void darkenedPlayersBarsDarkenTheWorldUntilTheyBrighten() {
        ServerMock server = MockBukkit.mock();
        try {
            World world = server.addSimpleWorld("world");
            PlayerMock player = server.addPlayer();
            player.teleport(world.getSpawnLocation());
            NightBars bars = new NightBars();
            bars.update(player, new BossBarSettings(), new NightTimes(13_000, 23_000), 0, "bloodnight.night.name");

            bars.darken(player, true);
            assertTrue(flags(player).contains(BossBar.Flag.DARKEN_SCREEN));
            bars.update(player, new BossBarSettings(), new NightTimes(13_000, 23_000), 0, "bloodnight.night.name");
            assertTrue(flags(player).contains(BossBar.Flag.DARKEN_SCREEN));

            bars.darken(player, false);
            assertFalse(flags(player).contains(BossBar.Flag.DARKEN_SCREEN));
            assertTrue(flags(player).contains(BossBar.Flag.CREATE_WORLD_FOG));
        } finally {
            MockBukkit.unmock();
        }
    }

    private static Set<BossBar.Flag> flags(PlayerMock player) {
        Set<BossBar.Flag> flags = new HashSet<>();
        player.activeBossBars().forEach(bar -> flags.addAll(bar.flags()));
        return flags;
    }
}
