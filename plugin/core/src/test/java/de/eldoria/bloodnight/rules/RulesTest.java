/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.time.Clock;
import java.util.List;
import java.util.logging.Logger;

/**
 * A server with one world in which every night is a blood night, for the rules' tests.
 */
public abstract class RulesTest {
    protected static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    protected ServerMock server;
    protected Plugin plugin;
    protected World world;
    protected BloodNights nights;
    protected WorldSettings settings = Settings.enabled();

    @BeforeEach
    protected void setUpServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        var every = Settings.everyNight();
        nights = new BloodNights(
                new NightSelector(ignored -> every, ignored -> TIMES, Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
    }

    @AfterEach
    protected void tearDownServer() {
        MockBukkit.unmock();
    }

    protected void bloodNight() {
        at(13_000);
    }

    protected void day() {
        at(24_000);
    }

    private void at(long fullTime) {
        world.setFullTime(fullTime);
        nights.check(List.of(world));
    }
}
