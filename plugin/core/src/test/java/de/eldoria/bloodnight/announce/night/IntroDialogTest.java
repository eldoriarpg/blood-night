/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce.night;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.configuration.world.NightSelection;
import de.eldoria.bloodnight.configuration.world.presentation.AnnouncementSettings;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntroDialogTest {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
            .build();
    private final List<Player> shown = new ArrayList<>();
    private final List<Runnable> waiting = new ArrayList<>();
    private ServerMock server;
    private World world;
    private BloodNights nights;
    private AnnouncementSettings settings = new AnnouncementSettings();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        NightSelection every = MAPPER.readValue(
                "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}", NightSelection.class);
        nights = new BloodNights(
                new NightSelector(
                        ignored -> every, ignored -> new NightTimes(13_000, 23_000), Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
        server.getPluginManager()
                .registerEvents(
                        new IntroDialog(
                                nights, ignored -> settings, (entity, ticks, work) -> waiting.add(work), shown::add),
                        MockBukkit.createMockPlugin());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private PlayerMock player() {
        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        later();
        return player;
    }

    private void later() {
        List<Runnable> due = List.copyOf(waiting);
        waiting.clear();
        due.forEach(Runnable::run);
    }

    private void at(long time) {
        world.setFullTime(time);
        nights.check(List.of(world));
        later();
    }

    @Test
    void playersSeeItOnTheirFirstBloodNightOnly() {
        PlayerMock player = player();
        at(13_000);
        at(24_000);
        at(13_000 + 24_000);

        assertEquals(List.of(player), shown);
    }

    @Test
    void playersJoiningDuringTheNightSeeIt() {
        at(13_000);
        PlayerMock player = player();

        assertTrue(shown.contains(player));
    }

    @Test
    void nobodySeesItOutsideBloodNights() {
        player();

        assertTrue(shown.isEmpty());
    }

    @Test
    void worldsCanSwitchItOff() {
        settings = MAPPER.readValue("{\"introDialog\":false}", AnnouncementSettings.class);
        player();
        at(13_000);

        assertTrue(shown.isEmpty());
    }
}
