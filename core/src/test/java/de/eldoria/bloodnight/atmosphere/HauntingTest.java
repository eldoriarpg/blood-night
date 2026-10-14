/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.WorldBorder;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HauntingTest extends AtmosphereTest {
    private Haunting haunting;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        haunting = new Haunting(server, nights, ignored -> atmosphere, new Tasks(plugin), HauntingTest::border);
        server.getPluginManager().registerEvents(haunting, plugin);
        player = new BorderedPlayer(server);
        server.addPlayer(player);
        player.teleport(world.getSpawnLocation());
    }

    @Test
    void playersAreHauntedDuringTheNightOnly() {
        bloodNight();
        haunting.tick();
        assertNotNull(player.getWorldBorder());
        assertEquals(WeatherType.DOWNFALL, player.getPlayerWeather());

        day();
        haunting.tick();
        assertNull(player.getWorldBorder());
        assertNull(player.getPlayerWeather());
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
}
