/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodNightsTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-11T12:00:00Z"), ZoneOffset.UTC);
    private final List<String> events = new ArrayList<>();
    private final List<World> ended = new ArrayList<>();
    private ServerMock server;
    private Plugin plugin;
    private World world;
    private NightSelection selection;
    private NightSelector selector;
    private BloodNights nights;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        selection = Selections.of("{\"type\":\"INTERVAL\",\"interval\":{\"interval\":3,\"probability\":100}}");
        selector = new NightSelector(ignored -> selection, CLOCK, () -> 20);
        nights = new BloodNights(selector, server.getPluginManager(), ended::add, Logger.getLogger("test"));
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
        NightSelector again = new NightSelector(ignored -> selection, CLOCK, () -> 20);
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
        assertEquals(List.of(world), ended);
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
}
