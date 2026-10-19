/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.configuration.server.Announcements;
import de.eldoria.bloodnight.configuration.world.NightSelection;
import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.tasks.Tasks;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncerTest {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .build();
    private static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    private WorldSettings settings = new WorldSettings();
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
                selector, candidate -> candidate.equals(world), server.getPluginManager(), Logger.getLogger("test"));
        reach("SERVER");
        var plugin = MockBukkit.createMockPlugin();
        Texts.create(plugin, "<dark_red>[BN]</dark_red>", "en_US");
        announcer = new Announcer(
                server,
                nights,
                () -> announcements,
                ignored -> settings,
                ignored -> TIMES,
                new Tasks(plugin),
                target -> Component.text(target.getName()),
                RegionRules.NONE);
        server.getPluginManager().registerEvents(announcer, plugin);
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

    private static String plain(Component component) {
        assertNotNull(component);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void beginAndEndAreAnnouncedWithBarEffectAndSounds() {
        PlayerMock player = player(world);
        at(13_000);
        assertEquals("[BN] A blood night has begun in world.", plain(player.nextComponentMessage()));
        assertTrue(player.activeBossBars().iterator().hasNext());
        assertNotNull(player.getPotionEffect(PotionEffectType.DARKNESS));
        assertFalse(player.getHeardSounds().isEmpty());
        at(18_000);
        assertEquals(0.5f, player.activeBossBars().iterator().next().progress());
        player.removePotionEffect(PotionEffectType.DARKNESS);
        at(23_000);
        assertEquals("[BN] The blood night in world is over.", plain(player.nextComponentMessage()));
        assertFalse(player.activeBossBars().iterator().hasNext());
        assertNotNull(player.getPotionEffect(PotionEffectType.DARKNESS));
    }

    @Test
    void worldReachSkipsOtherWorlds() {
        reach("WORLD");
        PlayerMock inside = player(world);
        PlayerMock outside = player(other);
        at(13_000);
        assertEquals("[BN] A blood night has begun in world.", plain(inside.nextComponentMessage()));
        assertNull(outside.nextComponentMessage());
        assertFalse(outside.activeBossBars().iterator().hasNext());
    }

    @Test
    void playersChangingWorldsAreWarned() {
        reach("NONE");
        PlayerMock player = player(other);
        at(13_000);
        player.teleport(world.getSpawnLocation());
        assertEquals("You entered a world with a running blood night.", plain(player.nextActionBar()));
        assertTrue(player.activeBossBars().iterator().hasNext());
        player.teleport(other.getSpawnLocation());
        assertEquals("You left the world with the running blood night.", plain(player.nextActionBar()));
        assertFalse(player.activeBossBars().iterator().hasNext());
    }

    @Test
    void clearHidesTheBars() {
        PlayerMock player = player(world);
        at(13_000);
        announcer.clear();
        assertFalse(player.activeBossBars().iterator().hasNext());
    }

    @Test
    void changedBarSettingsApplyToTheRunningNight() {
        PlayerMock player = player(world);
        at(13_000);
        settings = MAPPER.readValue(
                "{\"announcement\":{\"bossBar\":{\"title\":\"changed\",\"color\":\"BLUE\",\"flags\":[]}}}",
                WorldSettings.class);
        at(14_000);
        BossBar bar = player.activeBossBars().iterator().next();
        assertEquals(Component.text("changed"), bar.name());
        assertEquals(BossBar.Color.BLUE, bar.color());
        assertTrue(bar.flags().isEmpty());
    }
}
