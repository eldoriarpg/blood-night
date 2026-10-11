/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.configuration.elements.world.NightSettings;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.world.ClockTimeSkipEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightActionsTest {
    private static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    private final List<String> commands = new ArrayList<>();
    private ServerMock server;
    private World world;
    private NightSettings settings;
    private NightSelector selector;
    private BloodNights nights;
    private NightStretch stretch;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        settings = Selections.read("""
                {"length": {"mode": "EXTENDED", "seconds": 1000},
                 "beginCommands": ["record begin {world}", "record hello {player}"],
                 "endCommands": ["record end {world}"]}""", NightSettings.class);
        NightSelection always = Selections.of("{\"type\":\"RANDOM\",\"random\":{\"probability\":100}}");
        selector = new NightSelector(
                ignored -> always, ignored -> TIMES, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), () -> 20);
        nights = new BloodNights(
                selector, ignored -> true, server.getPluginManager(), ignored -> {}, Logger.getLogger("test"));
        stretch = new NightStretch(server, ignored -> settings, ignored -> TIMES);
        server.getPluginManager().registerEvents(new NightSkipGuard(nights, ignored -> settings.skippable()), plugin);
        server.getPluginManager().registerEvents(new NightCommands(server, ignored -> settings), plugin);
        server.getPluginManager().registerEvents(stretch, plugin);
        server.getCommandMap().register("test", new Command("record") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                commands.add(String.join(" ", args));
                return true;
            }
        });
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void beginNight() {
        world.setFullTime(13_000);
        nights.check(List.of(world));
    }

    @Test
    void sleepingCannotSkipABloodNight() {
        beginNight();
        TimeSkipEvent sleep = new TimeSkipEvent(world, ClockTimeSkipEvent.SkipReason.NIGHT_SKIP, 10_000);
        TimeSkipEvent command = new TimeSkipEvent(world, ClockTimeSkipEvent.SkipReason.COMMAND, 10_000);
        server.getPluginManager().callEvent(sleep);
        server.getPluginManager().callEvent(command);

        assertTrue(sleep.isCancelled());
        assertFalse(command.isCancelled());
    }

    @Test
    void ordinaryNightsCanBeSlept() {
        TimeSkipEvent sleep = new TimeSkipEvent(world, ClockTimeSkipEvent.SkipReason.NIGHT_SKIP, 10_000);
        server.getPluginManager().callEvent(sleep);

        assertFalse(sleep.isCancelled());
    }

    @Test
    void beginAndEndRunTheirCommands() {
        server.addPlayer("Alex");
        server.addPlayer("Steve");
        beginNight();
        nights.cancel(world);

        assertEquals(List.of("begin world", "hello Alex", "hello Steve", "end world"), commands);
    }

    @Test
    void theNightIsStretchedTickByTick() {
        beginNight();
        for (int tick = 0; tick < 4; tick++) stretch.tick();

        assertEquals(13_000 + 10_000 * 4 / 20_000, world.getFullTime());
        for (int tick = 4; tick < 20_000; tick++) stretch.tick();
        assertEquals(23_000, world.getFullTime());
    }

    @Test
    void aTimeChangeFromOutsideEndsTheStretch() {
        beginNight();
        stretch.tick();
        world.setFullTime(18_000);
        stretch.tick();
        stretch.tick();

        assertEquals(18_000, world.getFullTime());
    }
}
