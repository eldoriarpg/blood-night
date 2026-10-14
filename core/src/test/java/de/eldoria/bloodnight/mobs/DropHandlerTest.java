/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;
import de.eldoria.bloodnight.configuration.elements.ForeignMobSettings;
import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.world.VanillaDrops;
import de.eldoria.bloodnight.configuration.elements.world.WorldDrops;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.mob.meta.VanillaLoot;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropHandlerTest {
    private ServerMock server;
    private WorldMock world;
    private CustomMob gemmy;
    private boolean bloodNight;
    private WorldDrops worldDrops = WorldDrops.defaultDrops();
    private DropHandler handler;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        gemmy = gemmy(VanillaLoot.KEEP);
        ItemRegistry items = new ItemRegistry(null, null);
        handler = new DropHandler(
                id -> id.equals("gemmy") ? gemmy : null,
                world -> worldDrops,
                GlobalDrops::defaultDrops,
                items::item,
                world -> bloodNight,
                new ForeignMobs(ForeignMobSettings::new, () -> null),
                RandomGenerator.of("L64X128MixRandom"));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static CustomMob gemmy(VanillaLoot vanillaLoot) {
        CustomMob zombie = Mobs.zombie("gemmy", Graph.builder().build());
        return new CustomMob(
                zombie.id(),
                zombie.equipment(),
                zombie.attributes(),
                zombie.behaviour(),
                new MobDrops(
                        ConfigurationScope.MOB,
                        Map.of(1, 1),
                        List.of(ConfigurationScope.MOB),
                        List.of(new Drop("minecraft:diamond", 3, 1)),
                        ConfigurationScope.MOB,
                        ValueModifier.VALUE,
                        10,
                        vanillaLoot),
                zombie.extension(),
                zombie.spawn());
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
    void aPlayerKillAddsTheDropsToTheVanillaLoot() {
        EntityDeathEvent event = death(true);

        assertEquals(
                List.of(new ItemStack(Material.ROTTEN_FLESH), new ItemStack(Material.DIAMOND, 3)), event.getDrops());
        assertEquals(10, event.getDroppedExp());
    }

    @Test
    void otherDeathsKeepTheirVanillaLootOnly() {
        EntityDeathEvent event = death(false);

        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), event.getDrops());
        assertEquals(5, event.getDroppedExp());
    }

    @Test
    void specialMobsCanDropTheirVanillaLoot() {
        gemmy = gemmy(VanillaLoot.CLEAR);
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
        VanillaDrops vanilla = VanillaDrops.defaultDrops();
        worldDrops = withVanilla(new VanillaDrops(
                true, 2, false, ValueModifier.MULTIPLY, 2, EnumSet.of(EntityType.ZOMBIE), vanilla.excludedReasons()));

        EntityDeathEvent zombie = die(zombie(), true, new ItemStack(Material.ROTTEN_FLESH));
        Cow cow = world.spawn(new Location(world, 0, 64, 0), Cow.class);
        EntityDeathEvent animal = die(cow, true, new ItemStack(Material.BEEF));

        assertEquals(List.of(new ItemStack(Material.ROTTEN_FLESH)), zombie.getDrops());
        assertEquals(List.of(new ItemStack(Material.BEEF)), animal.getDrops());
    }

    @Test
    void ordinaryEnemiesCanRollTheWorldsDrops() {
        bloodNight = true;
        VanillaDrops vanilla = VanillaDrops.defaultDrops();
        worldDrops = withVanilla(new VanillaDrops(
                true, 1, true, ValueModifier.DEFAULT, 0, vanilla.excludedTypes(), vanilla.excludedReasons()));
        worldDrops = new WorldDrops(
                ConfigurationScope.WORLD,
                Map.of(1, 1),
                List.of(ConfigurationScope.WORLD),
                List.of(new Drop("minecraft:emerald", 1, 1)),
                worldDrops.xpScope(),
                worldDrops.xpModifier(),
                worldDrops.xpValue(),
                worldDrops.vanilla());

        EntityDeathEvent event = die(zombie(), true, new ItemStack(Material.ROTTEN_FLESH));

        assertTrue(event.getDrops().contains(new ItemStack(Material.EMERALD)));
        assertEquals(5, event.getDroppedExp());
    }

    private static WorldDrops withVanilla(VanillaDrops vanilla) {
        WorldDrops drops = WorldDrops.defaultDrops();
        return new WorldDrops(
                drops.weightsScope(),
                drops.weights(),
                drops.dropTables(),
                drops.dropList(),
                drops.xpScope(),
                drops.xpModifier(),
                drops.xpValue(),
                vanilla);
    }
}
