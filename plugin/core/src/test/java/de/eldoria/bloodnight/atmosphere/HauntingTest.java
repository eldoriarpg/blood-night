/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Zombie;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HauntingTest extends AtmosphereTest {
    private Haunting haunting;
    private PlayerMock player;
    private boolean steady;
    private int pulses;
    private boolean dark;

    @BeforeEach
    void setUp() {
        haunting = new Haunting(
                server,
                nights,
                ignored -> atmosphere,
                new Tasks(plugin),
                HauntingTest::border,
                RegionRules.NONE,
                ignored -> steady,
                (ignored, darken) -> {
                    if (darken) pulses++;
                    dark = darken;
                });
        server.getPluginManager().registerEvents(haunting, plugin);
        player = new BorderedPlayer(server);
        server.addPlayer(player);
        player.teleport(world.getSpawnLocation());
    }

    @Test
    void playersAreHauntedDuringTheNightOnly() {
        atmosphere = MAPPER.readValue("{\"rain\":true}", atmosphere.getClass());
        bloodNight();
        haunting.tick();
        assertNotNull(player.getWorldBorder());
        assertEquals(WeatherType.DOWNFALL, player.getPlayerWeather());

        day();
        haunting.tick();
        assertNull(player.getWorldBorder());
        assertNull(player.getPlayerWeather());
    }

    @Test
    void itDoesNotRainUnlessRainIsOn() {
        bloodNight();
        haunting.tick();
        assertNull(player.getPlayerWeather());
    }

    @Test
    void theHeartbeatOnlyComesWithMonstersNear() {
        bloodNight();
        for (int i = 0; i < 20; i++) haunting.tick();
        assertTrue(player.getHeardSounds().isEmpty());

        world.spawn(player.getLocation().add(3, 0, 0), Zombie.class);
        for (int i = 0; i < 20; i++) haunting.tick();
        assertFalse(player.getHeardSounds().isEmpty());
    }

    @Test
    void aDarknessPulseBrightensAgainAfterTwoSeconds() {
        bloodNight();
        while (pulses == 0) haunting.tick();
        assertTrue(dark);

        for (int i = 0; i < Haunting.PULSE_TICKS / Haunting.STEP; i++) haunting.tick();
        assertFalse(dark);
    }

    @Test
    void leavingTheNightEndsARunningPulse() {
        bloodNight();
        while (pulses == 0) haunting.tick();

        day();
        haunting.tick();
        assertFalse(dark);
    }

    @Test
    void aSteadyHeartHearsNoHeartbeat() {
        steady = true;
        bloodNight();
        world.spawn(player.getLocation().add(3, 0, 0), Zombie.class);
        for (int i = 0; i < 20; i++) haunting.tick();
        assertTrue(player.getHeardSounds().isEmpty());
        assertNotNull(player.getWorldBorder());
    }

    /**
     * A world border that only keeps its centre; MockBukkit cannot create borders.
     */
    private static WorldBorder border() {
        Location[] centre = {null};
        return (WorldBorder) Proxy.newProxyInstance(
                WorldBorder.class.getClassLoader(),
                new Class<?>[] {WorldBorder.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setCenter" -> {
                        centre[0] = args.length == 1
                                ? (Location) args[0]
                                : new Location(null, (double) args[0], 0, (double) args[1]);
                        yield null;
                    }
                    case "getCenter" -> centre[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }

    /**
     * A player that keeps the world border it is given; MockBukkit's players do not.
     */
    private static final class BorderedPlayer extends PlayerMock {
        private @Nullable WorldBorder border;

        private BorderedPlayer(ServerMock server) {
            super(server, "Haunted");
        }

        @Override
        public void setWorldBorder(@Nullable WorldBorder border) {
            this.border = border;
        }

        @Override
        public @Nullable WorldBorder getWorldBorder() {
            return border;
        }
    }

    @Test
    void theHeartbeatFadesWithDistance() {
        assertEquals(0.03f, Haunting.heartbeatVolume(0), 1e-6);
        assertEquals(0.45f, Haunting.heartbeatVolume(1), 1e-6);
        assertTrue(Haunting.heartbeatVolume(0.5) < (0.03f + 0.45f) / 2);
    }

    @Test
    void theHeartbeatIsLoudestWithinItsDistanceAndSilentAtItsRange() {
        assertEquals(1, Haunting.closeness(5, 30, 5), 1e-9);
        assertEquals(1, Haunting.closeness(2, 30, 5), 1e-9);
        assertEquals(0, Haunting.closeness(30, 30, 5), 1e-9);
        assertEquals(0.5, Haunting.closeness(17.5, 30, 5), 1e-9);
    }
}
