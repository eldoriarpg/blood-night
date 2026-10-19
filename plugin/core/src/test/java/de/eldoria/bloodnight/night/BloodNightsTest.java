/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.api.event.night.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.night.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.world.NightSelection;
import de.eldoria.bloodnight.configuration.world.NightSettings;
import de.eldoria.bloodnight.configuration.world.night.TimedNights;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.night.selection.Prediction;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodNightsTest {
    private static final NightTimes DEFAULT_TIMES = new NightTimes(13_000, 23_000);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-11T12:00:00Z"), ZoneOffset.UTC);
    private final List<String> events = new ArrayList<>();
    private ServerMock server;
    private Plugin plugin;
    private World world;
    private NightSelection selection;
    private NightSelector selector;
    private BloodNights nights;
    private boolean enabled = true;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        selection = Selections.of("{\"type\":\"INTERVAL\",\"interval\":{\"interval\":3,\"probability\":100}}");
        selector = new NightSelector(ignored -> selection, ignored -> DEFAULT_TIMES, CLOCK, () -> 20);
        nights = new BloodNights(selector, ignored -> enabled, server.getPluginManager(), Logger.getLogger("test"));
        server.getPluginManager()
                .registerEvents(
                        new Listener() {
                            @EventHandler
                            void begin(BloodNightBeginEvent event) {
                                events.add("begin " + event.night());
                            }

                            @EventHandler
                            void end(BloodNightEndEvent event) {
                                events.add("end " + event.night());
                            }
                        },
                        plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void at(long fullTime) {
        world.setFullTime(fullTime);
        nights.check(List.of(world));
    }

    @Test
    void theSameNightGetsTheSameAnswer() {
        selection = Selections.of("{\"type\":\"RANDOM\",\"random\":{\"probability\":50}}");
        List<Boolean> first = new ArrayList<>();
        for (long night = 0; night < 200; night++) first.add(selector.isBloodNight(world, night));
        NightSelector again = new NightSelector(ignored -> selection, ignored -> DEFAULT_TIMES, CLOCK, () -> 20);
        for (long night = 0; night < 200; night++)
            assertEquals(first.get((int) night), again.isBloodNight(world, night));
        long bloodNights = first.stream().filter(Boolean::booleanValue).count();
        assertTrue(bloodNights > 70 && bloodNights < 130, "about half of 200: " + bloodNights);
    }

    @Test
    void theNextBloodNightIsPredicted() {
        world.setFullTime(24_000 + 1_000);
        Optional<Prediction> next = selector.next(world, 100);

        assertTrue(next.isPresent());
        assertEquals(3, next.get().night());
        assertEquals(3 * 24_000 + 13_000 - 25_000, next.get().ticksUntil());
    }

    @Test
    void overridesWinOverTheRoll() {
        world.setFullTime(24_000);
        selector.force(world, 1);
        selector.cancel(world, 3);

        assertTrue(selector.isBloodNight(world, 1));
        assertFalse(selector.isBloodNight(world, 3));
        assertEquals(1, selector.next(world, 100).orElseThrow().night());
    }

    @Test
    void bloodNightsBeginAtNightAndEndAtSunrise() {
        at(24_000 + 1_000);
        at(24_000 + 13_500);
        at(24_000 + 14_000);
        assertTrue(nights.running(world).isEmpty());

        at(3 * 24_000 + 13_500);
        assertEquals(3, nights.running(world).getAsLong());
        at(3 * 24_000 + 23_500);

        assertEquals(List.of("begin 3", "end 3"), events);
        assertTrue(nights.running(world).isEmpty());
    }

    @Test
    void aJumpInTimeEndsTheRunningBloodNight() {
        at(13_500);
        at(24_000 + 15_000);
        at(3 * 24_000 + 15_000);

        assertEquals(List.of("begin 0", "end 0", "begin 3"), events);
    }

    @Test
    void aCancelledBeginLeavesTheNightOrdinary() {
        server.getPluginManager()
                .registerEvents(
                        new Listener() {
                            @EventHandler
                            void cancel(BloodNightBeginEvent event) {
                                event.setCancelled(true);
                            }
                        },
                        plugin);

        at(13_500);
        at(14_000);

        assertTrue(nights.running(world).isEmpty());
        assertEquals(List.of("begin 0"), events);
    }

    @Test
    void forcingDuringTheNightBeginsNowAndCancellingEndsIt() {
        at(24_000 + 14_000);
        assertTrue(nights.running(world).isEmpty());

        assertEquals(1, nights.force(world));
        assertEquals(1, nights.running(world).getAsLong());

        assertEquals(1, nights.cancel(world));
        assertTrue(nights.running(world).isEmpty());
        at(24_000 + 15_000);
        assertEquals(List.of("begin 1", "end 1"), events);
    }

    @Test
    void overridesOfPastNightsAreForgotten() {
        selector.force(world, 1);
        at(4 * 24_000 + 14_000);

        world.setFullTime(0);
        assertFalse(selector.isBloodNight(world, 1));
    }

    @Test
    void disabledWorldsHaveNoBloodNights() {
        enabled = false;
        at(13_500);
        assertTrue(nights.running(world).isEmpty());

        enabled = true;
        at(14_000);
        assertEquals(0, nights.running(world).getAsLong());
        enabled = false;
        at(14_500);

        assertTrue(nights.running(world).isEmpty());
        assertEquals(List.of("begin 0", "end 0"), events);
    }

    @Test
    void worldsWithoutADayCycleHaveTimedNights() {
        WorldMock end = new WorldMock();
        end.setName("the_end");
        end.setEnvironment(World.Environment.THE_END);
        server.addWorld(end);
        NightSettings settings = Selections.read(
                "{\"timed\":{\"everyMinutes\":10,\"chance\":100,\"lengthMinutes\":2}}", NightSettings.class);
        TimedNights timed = settings.timed();
        NightTimes times = NightTimes.of(end, settings);
        BloodNights timedNights = new BloodNights(
                new NightSelector(ignored -> timed, ignored -> times, CLOCK, () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));

        end.setGameTime(3 * 12_000 + 100);
        timedNights.check(List.of(end));
        assertEquals(3, timedNights.running(end).getAsLong());
        assertEquals(new NightTimes(0, 2_400, 12_000), times);

        end.setGameTime(3 * 12_000 + 2_400);
        timedNights.check(List.of(end));
        assertTrue(timedNights.running(end).isEmpty());
    }

    @Test
    void forcingInAWorldWithoutADayCycleBeginsNowForTheLengthOfANight() {
        WorldMock end = new WorldMock();
        end.setName("the_end");
        end.setEnvironment(World.Environment.THE_END);
        server.addWorld(end);
        NightSettings settings = Selections.read(
                "{\"timed\":{\"everyMinutes\":10,\"chance\":0,\"lengthMinutes\":2}}", NightSettings.class);
        TimedNights timed = settings.timed();
        BloodNights timedNights = new BloodNights(
                new NightSelector(ignored -> timed, ignored -> NightTimes.of(end, settings), CLOCK, () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));

        end.setGameTime(3 * 12_000 + 5_000);
        assertEquals(3, timedNights.force(end));
        assertEquals(3, timedNights.running(end).getAsLong());
        NightTimes times = NightTimes.of(end, settings);
        assertEquals(3 * 12_000 + 5_000 + times.length(), timedNights.endOf(end, times, 3));

        end.setGameTime(3 * 12_000 + 7_000);
        timedNights.check(List.of(end));
        assertEquals(3, timedNights.running(end).getAsLong());

        end.setGameTime(3 * 12_000 + 7_400);
        timedNights.check(List.of(end));
        assertTrue(timedNights.running(end).isEmpty());

        end.setGameTime(4 * 12_000 + 100);
        timedNights.check(List.of(end));
        assertTrue(timedNights.running(end).isEmpty());
    }

    @Test
    void heldBloodNightsLastUntilReleased() {
        world.setFullTime(1_000);
        nights.hold(world, () -> 1);
        assertTrue(nights.running(world).isPresent());

        world.setFullTime(5_000);
        nights.check(List.of(world));
        assertTrue(nights.running(world).isPresent());

        nights.release(world);
        assertTrue(nights.running(world).isEmpty());
    }

    @Test
    void aHoldThatEndsTheNightFillsTheBar() {
        world.setFullTime(1_000);
        nights.hold(world, () -> 0.4);
        assertEquals(0.4, nights.holdLeft(world).orElseThrow(), 1e-9);

        nights.release(world);
        assertTrue(nights.holdLeft(world).isEmpty());
    }

    @Test
    void aForcedNightRunsOutItsTimeAfterTheHoldLetsGo() {
        WorldMock end = new WorldMock();
        end.setName("the_end");
        end.setEnvironment(World.Environment.THE_END);
        server.addWorld(end);
        NightSettings settings = Selections.read(
                "{\"timed\":{\"everyMinutes\":10,\"chance\":0,\"lengthMinutes\":2}}", NightSettings.class);
        TimedNights timed = settings.timed();
        BloodNights timedNights = new BloodNights(
                new NightSelector(ignored -> timed, ignored -> NightTimes.of(end, settings), CLOCK, () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));

        end.setGameTime(3 * 12_000 + 5_000);
        timedNights.force(end);
        timedNights.hold(end, () -> 0.5);
        assertTrue(timedNights.holdLeft(end).isEmpty());

        end.setGameTime(3 * 12_000 + 5_500);
        timedNights.release(end);
        timedNights.check(List.of(end));
        assertTrue(timedNights.running(end).isPresent());

        end.setGameTime(3 * 12_000 + 7_500);
        timedNights.check(List.of(end));
        assertTrue(timedNights.running(end).isEmpty());
    }
}
