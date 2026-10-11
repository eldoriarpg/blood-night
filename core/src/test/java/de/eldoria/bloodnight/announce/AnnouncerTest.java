/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.configuration.elements.Announcements;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightTimes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncerTest {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .build();
    private static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    private final WorldSettings settings = new WorldSettings();
    private ServerMock server;
    private World world;
    private World other;
    private BloodNights nights;
    private Announcer announcer;
    private Announcements announcements;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        other = server.addSimpleWorld("other");
        NightSelection always = MAPPER.readValue(
                "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}", NightSelection.class);
        NightSelector selector = new NightSelector(ignored -> always, ignored -> TIMES, Clock.systemUTC(), () -> 20);
        nights = new BloodNights(
                selector,
                candidate -> candidate.equals(world),
                server.getPluginManager(),
                ignored -> {},
                Logger.getLogger("test"));
        reach("SERVER");
        announcer = new Announcer(server, nights, () -> announcements, ignored -> settings, ignored -> TIMES);
        server.getPluginManager().registerEvents(announcer, MockBukkit.createMockPlugin());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void reach(String reach) {
        announcements = MAPPER.readValue(
                "{\"reach\":\"%s\",\"method\":\"CHAT\",\"personalMethod\":\"ACTION_BAR\"}".formatted(reach),
                Announcements.class);
    }

    private void at(long fullTime) {
        world.setFullTime(fullTime);
        other.setFullTime(fullTime);
        nights.check(List.of(world, other));
        announcer.tick();
    }

    private PlayerMock player(World in) {
        PlayerMock player = server.addPlayer();
        player.teleport(in.getSpawnLocation());
        return player;
    }

    private static String key(Component component) {
        assertNotNull(component);
        TranslatableComponent translatable = assertInstanceOf(
                TranslatableComponent.class,
                component.children().isEmpty()
                        ? component
                        : component.children().getLast());
        return translatable.key();
    }

    @Test
    void beginAndEndAreAnnouncedWithBarEffectAndSounds() {
        PlayerMock player = player(world);
        at(13_000);
        assertEquals(Messages.NIGHT_BEGIN, key(player.nextComponentMessage()));
        assertTrue(player.activeBossBars().iterator().hasNext());
        assertNotNull(player.getPotionEffect(PotionEffectType.DARKNESS));
        assertFalse(player.getHeardSounds().isEmpty());
        at(18_000);
        assertEquals(0.5f, player.activeBossBars().iterator().next().progress());
        at(23_000);
        assertEquals(Messages.NIGHT_END, key(player.nextComponentMessage()));
        assertFalse(player.activeBossBars().iterator().hasNext());
    }

    @Test
    void worldReachSkipsOtherWorlds() {
        reach("WORLD");
        PlayerMock inside = player(world);
        PlayerMock outside = player(other);
        at(13_000);
        assertEquals(Messages.NIGHT_BEGIN, key(inside.nextComponentMessage()));
        assertNull(outside.nextComponentMessage());
        assertFalse(outside.activeBossBars().iterator().hasNext());
    }

    @Test
    void playersChangingWorldsAreWarned() {
        reach("NONE");
        PlayerMock player = player(other);
        at(13_000);
        player.teleport(world.getSpawnLocation());
        assertEquals(Messages.NIGHT_JOINED, key(player.nextActionBar()));
        assertTrue(player.activeBossBars().iterator().hasNext());
        player.teleport(other.getSpawnLocation());
        assertEquals(Messages.NIGHT_LEFT, key(player.nextActionBar()));
        assertFalse(player.activeBossBars().iterator().hasNext());
    }

    @Test
    void clearHidesTheBars() {
        PlayerMock player = player(world);
        at(13_000);
        announcer.clear();
        assertFalse(player.activeBossBars().iterator().hasNext());
    }
}
