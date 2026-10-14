/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.placeholders;

import de.eldoria.bloodnight.api.BloodNightService;
import de.eldoria.bloodnight.api.NightForecast;
import de.eldoria.bloodnight.api.RunningNight;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlaceholdersTest {
    private ServerMock server;
    private World world;
    private World nether;
    private boolean reveal;
    private Placeholders placeholders;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        nether = server.addSimpleWorld("world_nether");
        placeholders = new Placeholders(new Service(), server::getWorld, () -> 20, () -> reveal);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private String value(String parameter, World playerWorld) {
        return placeholders.value(Placeholders.parse(parameter), playerWorld);
    }

    @Test
    void parametersNameAPlaceholderNightsAheadAndAWorld() {
        assertEquals(new Placeholders.Request("active", List.of()), Placeholders.parse("active"));
        assertEquals(
                new Placeholders.Request("active", List.of("world_nether")), Placeholders.parse("active_world_nether"));
        assertEquals(
                new Placeholders.Request("probability", List.of("2", "world_nether")),
                Placeholders.parse("probability_2_world_nether"));
        assertEquals(new Placeholders.Request("seconds_left", List.of()), Placeholders.parse("seconds_left"));
        assertNull(Placeholders.parse("unknown"));
    }

    @Test
    void valuesComeFromTheService() {
        assertEquals("true", value("active", world));
        assertEquals("false", value("active_world_nether", world));
        assertEquals("30", value("probability_2", world));
        assertEquals("70", value("probability_world_nether", world));
        assertEquals("0:05:00", value("time_left", world));
        assertEquals("300", value("seconds_left", world));
        assertEquals("25.0", value("percent_left", world));
        assertEquals("0", value("seconds_left", nether));
        assertNull(placeholders.value(Placeholders.parse("active"), null));
    }

    @Test
    void theNextBloodNightStaysSecretUnlessRevealed() {
        assertEquals("", value("next_nights", world));
        reveal = true;
        assertEquals("3", value("next_nights", world));
        assertEquals("1:00:00", value("next_time", world));
    }

    /**
     * A blood night runs in "world" with five minutes left; the next one comes three nights later, in an hour.
     */
    private final class Service implements BloodNightService {
        @Override
        public boolean isRunning(World target) {
            return target.equals(world);
        }

        @Override
        public Optional<RunningNight> running(World target) {
            return isRunning(target) ? Optional.of(new RunningNight(4, 6000, 0.25, false)) : Optional.empty();
        }

        @Override
        public Optional<NightForecast> next(World target, int horizon) {
            return Optional.of(new NightForecast(7, 3, 72_000, false));
        }

        @Override
        public int probability(World target, int nightsAhead) {
            return target.equals(world) ? 10 + 10 * nightsAhead : 70;
        }

        @Override
        public void force(World target) {}

        @Override
        public void cancel(World target) {}

        @Override
        public Set<World> runningWorlds() {
            return Set.of(world);
        }

        @Override
        public boolean isCustomMob(Entity entity) {
            return false;
        }

        @Override
        public Optional<String> customMobId(Entity entity) {
            return Optional.empty();
        }

        @Override
        public Set<String> customMobIds() {
            return Set.of();
        }

        @Override
        public Optional<LivingEntity> spawn(String mobId, Location location) {
            return Optional.empty();
        }
    }
}
