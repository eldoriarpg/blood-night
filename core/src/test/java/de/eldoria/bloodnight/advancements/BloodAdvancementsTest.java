/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.advancements;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.enchantments.BloodEnchantments;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightTimes;
import de.eldoria.bloodnight.store.BuiltInStore;
import de.eldoria.bloodnight.store.StoreEntry;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodAdvancementsTest {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
            .build();
    private final List<String> granted = new ArrayList<>();
    private ServerMock server;
    private World world;
    private BloodNights nights;
    private PlayerMock player;
    private boolean enabled = true;

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
                        new BloodAdvancements(
                                nights,
                                () -> enabled,
                                Set.of("speed_zombie", "blood_dragon"),
                                "blood_dragon",
                                (player, key, criterion) -> granted.add(key.value() + ":" + criterion),
                                BloodAdvancementsTest::level),
                        MockBukkit.createMockPlugin());
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static int level(ItemStack item, NamespacedKey enchantment) {
        return enchantment.equals(BloodEnchantments.NIGHT_WARD) && item.getType() == Material.NETHERITE_CHESTPLATE
                ? 5
                : 0;
    }

    private void at(long time) {
        world.setFullTime(time);
        nights.check(List.of(world));
    }

    private void kill(String id) {
        Zombie zombie = world.spawn(world.getSpawnLocation(), Zombie.class);
        MobTags.markCustomMob(zombie, Mobs.zombie(id, Graph.builder().build()));
        zombie.setKiller(player);
        server.getPluginManager()
                .callEvent(new EntityDeathEvent(
                        zombie, DamageSource.builder(DamageType.PLAYER_ATTACK).build(), new ArrayList<>(), 0));
    }

    @Test
    void survivingANightGrantsTheTabAndTheAdvancement() {
        at(13_000);
        at(24_000);

        assertTrue(granted.contains("blood_night/root:granted"));
        assertTrue(granted.contains("blood_night/survive:granted"));
    }

    @Test
    void tenKillsInOneNightMakeASlayer() {
        at(13_000);
        for (int i = 0; i < BloodAdvancements.SLAYER_KILLS - 1; i++) kill("speed_zombie");
        assertFalse(granted.contains("blood_night/slayer:granted"));

        kill("speed_zombie");
        assertTrue(granted.contains("blood_night/slayer:granted"));
    }

    @Test
    void killsOfAnEarlierNightDoNotCount() {
        at(13_000);
        for (int i = 0; i < BloodAdvancements.SLAYER_KILLS - 1; i++) kill("speed_zombie");
        at(24_000);
        at(13_000 + 24_000);
        kill("speed_zombie");

        assertFalse(granted.contains("blood_night/slayer:granted"));
    }

    @Test
    void storeMobsCountForTheCollectorOthersDoNot() {
        kill("speed_zombie");
        kill("my_own_mob");

        assertTrue(granted.contains("blood_night/collector:speed_zombie"));
        assertFalse(granted.contains("blood_night/collector:my_own_mob"));
    }

    @Test
    void everyoneNearTheBloodDragonSharesTheKill() {
        PlayerMock far = server.addPlayer();
        far.teleport(world.getSpawnLocation().add(1000, 0, 0));
        kill("blood_dragon");

        assertEquals(
                1,
                granted.stream()
                        .filter("blood_night/blood_dragon:granted"::equals)
                        .count());
    }

    @Test
    void nightWardFiveIsWorthAnAdvancement() {
        server.getPluginManager()
                .callEvent(new PlayerArmorChangeEvent(
                        player,
                        PlayerArmorChangeEvent.SlotType.CHEST,
                        ItemStack.empty(),
                        new ItemStack(Material.NETHERITE_CHESTPLATE)));

        assertTrue(granted.contains("blood_night/night_ward:granted"));
    }

    @Test
    void switchedOffNothingIsGranted() {
        enabled = false;
        at(13_000);
        at(24_000);
        kill("speed_zombie");

        assertTrue(granted.isEmpty());
    }

    @Test
    void theCollectorAsksForEveryStoreMob() throws IOException {
        Set<String> store =
                BuiltInStore.load().entries().stream().map(StoreEntry::id).collect(Collectors.toSet());
        JsonNode collector = read("/datapack/data/bloodnight/advancement/blood_night/collector.json");
        Set<String> criteria = new TreeSet<>(collector.path("criteria").propertyNames());

        assertEquals(new TreeSet<>(store), criteria);
    }

    @Test
    void everyAdvancementTheCodeGrantsExists() {
        for (NamespacedKey key : List.of(
                BloodAdvancements.ROOT,
                BloodAdvancements.SURVIVE,
                BloodAdvancements.SLAYER,
                BloodAdvancements.COLLECTOR,
                BloodAdvancements.BLOOD_DRAGON,
                BloodAdvancements.NIGHT_WARD)) {
            assertTrue(
                    BloodAdvancementsTest.class.getResource(
                                    "/datapack/data/bloodnight/advancement/%s.json".formatted(key.value()))
                            != null,
                    key.toString());
        }
    }

    private static JsonNode read(String resource) throws IOException {
        try (InputStream in = Objects.requireNonNull(BloodAdvancementsTest.class.getResourceAsStream(resource))) {
            return MAPPER.readTree(in);
        }
    }
}
