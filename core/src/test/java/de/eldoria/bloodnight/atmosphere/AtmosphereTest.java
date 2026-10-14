/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.configuration.elements.world.AtmosphereSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightTimes;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.List;
import java.util.logging.Logger;

/**
 * A server with one world in which every night is a blood night, for the atmosphere's tests.
 */
abstract class AtmosphereTest {
    static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .build();
    static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    ServerMock server;
    Plugin plugin;
    World world;
    BloodNights nights;
    AtmosphereSettings atmosphere = new AtmosphereSettings();

    @BeforeEach
    void setUpServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        NightSelection every = MAPPER.readValue(
                "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}", NightSelection.class);
        nights = new BloodNights(
                new NightSelector(ignored -> every, ignored -> TIMES, Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
    }

    @AfterEach
    void tearDownServer() {
        MockBukkit.unmock();
    }

    void bloodNight() {
        at(13_000);
    }

    void day() {
        at(24_000);
    }

    private void at(long fullTime) {
        world.setFullTime(fullTime);
        nights.check(List.of(world));
    }
}
