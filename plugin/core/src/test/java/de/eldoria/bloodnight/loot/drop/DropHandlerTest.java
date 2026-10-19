/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.drop;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.api.event.mob.BloodNightLootEvent;
import de.eldoria.bloodnight.configuration.server.plugins.ForeignMobSettings;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.items.ItemTexts;
import de.eldoria.bloodnight.living.spawn.ForeignMobs;
import de.eldoria.bloodnight.loot.LootAmount;
import de.eldoria.bloodnight.loot.LootEntry;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.loot.LootWear;
import de.eldoria.bloodnight.loot.assign.Experience;
import de.eldoria.bloodnight.loot.assign.MobLoot;
import de.eldoria.bloodnight.loot.assign.OrdinaryLoot;
import de.eldoria.bloodnight.loot.assign.ServerLoot;
import de.eldoria.bloodnight.loot.assign.VanillaLoot;
import de.eldoria.bloodnight.loot.assign.WorldLoot;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.attributes.ValueModifier;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropHandlerTest {
    private static final Map<String, LootTable> TABLES = Map.of(
            "gems", table("minecraft:diamond", LootAmount.exactly(3)),
            "emeralds", table("minecraft:emerald", LootAmount.exactly(1)),
            "pearls", table("minecraft:ender_pearl", LootAmount.exactly(40)),
            "worn",
                    new LootTable(
                            "Worn",
                            List.of(new LootEntry(
                                    "minecraft:diamond_sword",
                                    1,
                                    LootAmount.exactly(1),
                                    false,
                                    new LootWear(0.5, 0.5)))));

    private ServerMock server;
    private WorldMock world;
    private CustomMob gemmy;
    private boolean bloodNight;
    private WorldLoot worldLoot = WorldLoot.defaults();
    private ServerLoot serverLoot = new ServerLoot(List.of("emeralds"), Map.of(1, 1), null, null);
    private Consumer<Event> events = event -> {};
    private DropHandler handler;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        gemmy = gemmy(VanillaLoot.KEEP, false);
        ItemRegistry items = new ItemRegistry(null, null);
        handler = new DropHandler(
                id -> id.equals("gemmy") ? gemmy : null,
                world -> worldLoot,
                () -> serverLoot,
                TABLES::get,
                items::item,
                world -> bloodNight,
                new ForeignMobs(ForeignMobSettings::new, () -> null),
                RandomGenerator.of("L64X128MixRandom"),
                RegionRules.NONE,
                (mob, killer) -> List.of(new ItemStack(Material.NETHER_STAR)),
                event -> events.accept(event),
                Logger.getAnonymousLogger());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static LootTable table(String item, LootAmount amount) {
        return new LootTable(item, List.of(new LootEntry(item, 1, amount, false)));
    }

    private static CustomMob gemmy(VanillaLoot vanillaLoot, boolean inherit) {
        return Mobs.zombie("gemmy", Graph.builder().build())
                .withLoot(new MobLoot(
                        List.of("gems"),
                        Map.of(1, 1),
                        inherit,
                        true,
                        vanillaLoot,
                        new Experience(ValueModifier.VALUE, 10)));
    }

    private EntityDeathEvent die(LivingEntity entity, boolean byPlayer, ItemStack... drops) {
        if (byPlayer) entity.setKiller(server.addPlayer());
        EntityDeathEvent event = new EntityDeathEvent(
                entity, DamageSource.builder(DamageType.GENERIC).build(), new ArrayList<>(List.of(drops)), 5);
        handler.onDeath(event);
        return event;
    }

    private Zombie zombie() {
        return world.spawn(new Location(world, 0, 64, 0), Zombie.class);
    }

    private EntityDeathEvent death(boolean byPlayer) {
        Zombie zombie = zombie();
        MobTags.markCustomMob(zombie, gemmy);
        return die(zombie, byPlayer, new ItemStack(Material.ROTTEN_FLESH));
    }

    @Test
    void aPlayerKillAddsTheRolledLootToTheVanillaLoot() {
        EntityDeathEvent event = death(true);

        assertEquals(
                List.of(new ItemStack(Material.ROTTEN_FLESH), new ItemStack(Material.DIAMOND, 3)), event.getDrops());
        assertEquals(10, event.getDroppedExp());
    }

    @Test
    void inheritingMobsShareThePoolWithTheServer() {
        gemmy = gemmy(VanillaLoot.CLEAR, true);
        Set<List<ItemStack>> seen = new HashSet<>();

        for (int kill = 0; kill < 40; kill++) seen.add(death(true).getDrops());

        assertEquals(
                Set.of(List.of(new ItemStack(Material.DIAMOND, 3)), List.of(new ItemStack(Material.EMERALD))), seen);
    }

    @Test
    void theMostSpecificExperienceWins() {
        gemmy = Mobs.zombie("gemmy", Graph.builder().build()).withLoot(MobLoot.inherited());

        EntityDeathEvent event = death(true);

        assertEquals(20, event.getDroppedExp());
    }

    @Test
    void entriesDropTheirItemAsWornAsTheyRoll() {
        gemmy = Mobs.zombie("gemmy", Graph.builder().build())
                .withLoot(new MobLoot(List.of("worn"), Map.of(1, 1), false, true, VanillaLoot.CLEAR, null));

        ItemStack sword = death(true).getDrops().getFirst();

        assertEquals(Material.DIAMOND_SWORD, sword.getType());
        assertEquals(0.5, ItemTexts.read(sword).wear(), 0.01);
    }

    @Test
    void largeAmountsAreSplitIntoStacks() {
        assertEquals(
                List.of(new ItemStack(Material.ENDER_PEARL, 16), new ItemStack(Material.ENDER_PEARL, 4)),
                DropHandler.stacks(new ItemStack(Material.ENDER_PEARL), 20));
    }

    @Test
    void theLootEventCanChangeAndCancelTheRolledLoot() {
        events = event -> {
            if (event instanceof BloodNightLootEvent loot) {
                assertEquals("gemmy", loot.mobId());
                loot.loot().get("gems").set(0, new ItemStack(Material.GOLD_INGOT));
            }
        };
        assertEquals(
                List.of(new ItemStack(Material.ROTTEN_FLESH), new ItemStack(Material.GOLD_INGOT)),
                death(true).getDrops());

        events = event -> {
            if (event instanceof BloodNightLootEvent loot) loot.setCancelled(true);
        };
        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), death(true).getDrops());
    }

    @Test
    void specialMobsDropTheRewardDuringBloodNights() {
        bloodNight = true;

        EntityDeathEvent event = death(true);

        assertEquals(
                List.of(
                        new ItemStack(Material.ROTTEN_FLESH),
                        new ItemStack(Material.DIAMOND, 3),
                        new ItemStack(Material.NETHER_STAR)),
                event.getDrops());
    }

    @Test
    void worldsCanTurnTheRewardOff() {
        bloodNight = true;
        worldLoot = new WorldLoot(null, null, null, null, null, false, null);

        EntityDeathEvent event = death(true);

        assertEquals(
                List.of(new ItemStack(Material.ROTTEN_FLESH), new ItemStack(Material.DIAMOND, 3)), event.getDrops());
    }

    @Test
    void otherDeathsKeepTheirVanillaLootOnly() {
        EntityDeathEvent event = death(false);

        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), event.getDrops());
        assertEquals(5, event.getDroppedExp());
    }

    @Test
    void specialMobsCanDropTheirVanillaLoot() {
        gemmy = gemmy(VanillaLoot.CLEAR, false);
        Zombie zombie = zombie();
        zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        MobTags.markCustomMob(zombie, gemmy);

        EntityDeathEvent event =
                die(zombie, true, new ItemStack(Material.ROTTEN_FLESH), new ItemStack(Material.IRON_SWORD));

        assertEquals(List.of(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.DIAMOND, 3)), event.getDrops());
    }

    @Test
    void ordinaryEnemiesDropMoreDuringBloodNights() {
        bloodNight = true;
        Zombie zombie = zombie();
        zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));

        EntityDeathEvent event =
                die(zombie, true, new ItemStack(Material.ROTTEN_FLESH, 2), new ItemStack(Material.IRON_SWORD));

        assertEquals(
                List.of(new ItemStack(Material.ROTTEN_FLESH, 4), new ItemStack(Material.IRON_SWORD)), event.getDrops());
        assertEquals(10, event.getDroppedExp());
    }

    @Test
    void stacksStayWithinTheirSize() {
        bloodNight = true;

        EntityDeathEvent event = die(zombie(), true, new ItemStack(Material.ROTTEN_FLESH, 40));

        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH, 64)), event.getDrops());
    }

    @Test
    void ordinaryEnemiesKeepTheirLootOutsideBloodNightsAndWithoutAPlayer() {
        EntityDeathEvent outside = die(zombie(), true, new ItemStack(Material.ROTTEN_FLESH));
        bloodNight = true;
        EntityDeathEvent unkilled = die(zombie(), false, new ItemStack(Material.ROTTEN_FLESH));

        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), outside.getDrops());
        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), unkilled.getDrops());
        assertEquals(5, unkilled.getDroppedExp());
    }

    @Test
    void excludedKindsAndAnimalsKeepTheirLoot() {
        bloodNight = true;
        worldLoot = withOrdinary(new OrdinaryLoot(null, null, null, null, null, null, Set.of(EntityType.ZOMBIE), null));

        EntityDeathEvent zombie = die(zombie(), true, new ItemStack(Material.ROTTEN_FLESH));
        Cow cow = world.spawn(new Location(world, 0, 64, 0), Cow.class);
        EntityDeathEvent animal = die(cow, true, new ItemStack(Material.BEEF));

        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), zombie.getDrops());
        assertEquals(List.of(new ItemStack(Material.BEEF)), animal.getDrops());
    }

    @Test
    void ordinaryEnemiesRollTheirOwnTables() {
        bloodNight = true;
        worldLoot = withOrdinary(
                new OrdinaryLoot(null, 1.0, List.of("emeralds"), Map.of(1, 1), ValueModifier.DEFAULT, 0.0, null, null));

        EntityDeathEvent event = die(zombie(), true, new ItemStack(Material.ROTTEN_FLESH));

        assertTrue(event.getDrops().contains(new ItemStack(Material.EMERALD)));
        assertEquals(5, event.getDroppedExp());
    }

    private static WorldLoot withOrdinary(OrdinaryLoot ordinary) {
        return new WorldLoot(null, null, null, null, null, null, ordinary);
    }
}
