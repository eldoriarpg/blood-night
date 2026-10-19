/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.enchantments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.DamageEvents;
import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.api.event.mob.CustomMobMoveEntityEvent;
import de.eldoria.bloodnight.configuration.world.NightSelection;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.ArrowMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentEffectsTest {
    private static final Map<Material, Integer> WARD =
            Map.of(Material.DIAMOND_CHESTPLATE, 5, Material.DIAMOND_HELMET, 2);
    private ServerMock server;
    private World world;
    private BloodNights nights;
    private PlayerMock player;
    private Zombie zombie;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        JsonMapper mapper = JsonMapper.builder()
                .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build();
        NightSelection every = mapper.readValue(
                "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}", NightSelection.class);
        nights = new BloodNights(
                new NightSelector(
                        ignored -> every, ignored -> new NightTimes(13_000, 23_000), Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
        server.getPluginManager()
                .registerEvents(
                        new EnchantmentEffects(nights, EnchantmentEffectsTest::level, (entity, work) -> work.run()),
                        MockBukkit.createMockPlugin());
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        player.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        zombie = world.spawn(world.getSpawnLocation(), Zombie.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static int level(ItemStack item, NamespacedKey enchantment) {
        if (enchantment.equals(BloodEnchantments.NIGHT_WARD)) return WARD.getOrDefault(item.getType(), 0);
        if (enchantment.equals(BloodEnchantments.DAWNBRINGER)) return item.getType() == Material.GOLDEN_SWORD ? 4 : 0;
        if (enchantment.equals(BloodEnchantments.HUNTERS_MARK)) return item.getType() == Material.BOW ? 2 : 0;
        if (enchantment.equals(BloodEnchantments.EARTH_BOUND))
            return item.getType() == Material.NETHERITE_BOOTS ? 1 : 0;
        if (enchantment.equals(BloodEnchantments.ARCANE_VISION)) {
            return item.getType() == Material.NETHERITE_HELMET ? 1 : 0;
        }
        return enchantment.equals(BloodEnchantments.BLOODTHIRST) && item.getType() == Material.DIAMOND_SWORD ? 3 : 0;
    }

    private void bloodNight() {
        world.setFullTime(13_000);
        nights.check(List.of(world));
    }

    private double hit(Entity attacker) {
        var source = DamageSource.builder(DamageType.MOB_ATTACK)
                .withCausingEntity(attacker)
                .withDirectEntity(attacker)
                .build();
        var event = DamageEvents.byEntity(attacker, player, DamageCause.ENTITY_ATTACK, source, 10);
        server.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    private void kill(Entity victim) {
        ((LivingEntity) victim).setKiller(player);
        server.getPluginManager()
                .callEvent(new EntityDeathEvent(
                        (LivingEntity) victim,
                        DamageSource.builder(DamageType.PLAYER_ATTACK).build(),
                        new ArrayList<>(),
                        0));
    }

    @Test
    void nightWardTakesAShareOfMonsterDamageDuringBloodNights() {
        assertEquals(10, hit(zombie), 1e-9);
        bloodNight();
        assertEquals(10 * (1 - 7 * EnchantmentEffects.WARD_PER_LEVEL), hit(zombie), 1e-9);
        assertEquals(10, hit(world.spawn(world.getSpawnLocation(), Cow.class)), 1e-9);
    }

    @Test
    void wardNeverTakesMoreThanAllDamage() {
        assertEquals(0.5, EnchantmentEffects.warded(20), 1e-9);
        assertEquals(1, EnchantmentEffects.warded(100), 1e-9);
    }

    @Test
    void bloodthirstHealsOnKillsDuringBloodNights() {
        player.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        player.setHealth(10);
        kill(zombie);
        assertEquals(10, player.getHealth(), 1e-9);

        bloodNight();
        kill(world.spawn(world.getSpawnLocation(), Zombie.class));
        assertEquals(10 + 3 * EnchantmentEffects.BLOODTHIRST_HEAL_PER_LEVEL, player.getHealth(), 1e-9);

        player.setHealth(19);
        kill(world.spawn(world.getSpawnLocation(), Zombie.class));
        assertEquals(20, player.getHealth(), 1e-9);
    }

    @Test
    void otherWeaponsDoNotHeal() {
        player.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        player.setHealth(10);
        bloodNight();
        kill(zombie);
        assertEquals(10, player.getHealth(), 1e-9);
    }

    private double strike(LivingEntity victim) {
        var source = DamageSource.builder(DamageType.PLAYER_ATTACK)
                .withCausingEntity(player)
                .withDirectEntity(player)
                .build();
        var event = DamageEvents.byEntity(player, victim, DamageCause.ENTITY_ATTACK, source, 10);
        server.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    @Test
    void dawnbringerHitsSpecialMobsHarder() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
        Zombie special = world.spawn(world.getSpawnLocation(), Zombie.class);
        MobTags.markCustomMob(special, Mobs.zombie("special", Graph.builder().build()));

        assertEquals(10 * (1 + 4 * EnchantmentEffects.DAWNBRINGER_PER_LEVEL), strike(special), 1e-9);
        assertEquals(10, strike(zombie), 1e-9);
    }

    @Test
    void huntersMarkMakesArrowTargetsGlowDuringBloodNights() {
        Arrow arrow = new ArrowMock(server, UUID.randomUUID()) {
            @Override
            public ItemStack getWeapon() {
                return new ItemStack(Material.BOW);
            }
        };
        arrow.setShooter(player);
        var source = DamageSource.builder(DamageType.ARROW)
                .withCausingEntity(player)
                .withDirectEntity(arrow)
                .build();

        server.getPluginManager().callEvent(DamageEvents.byEntity(arrow, zombie, DamageCause.PROJECTILE, source, 4));
        assertFalse(zombie.hasPotionEffect(PotionEffectType.GLOWING));

        bloodNight();
        server.getPluginManager().callEvent(DamageEvents.byEntity(arrow, zombie, DamageCause.PROJECTILE, source, 4));
        assertEquals(
                2 * EnchantmentEffects.MARK_SECONDS_PER_LEVEL * 20,
                zombie.getPotionEffect(PotionEffectType.GLOWING).getDuration());
    }

    @Test
    void earthBoundBootsKeepPlayersFromBeingMoved() {
        var teleport = new CustomMobMoveEntityEvent(
                player,
                CustomMobMoveEntityEvent.Kind.TELEPORT,
                world.getSpawnLocation().add(0, 20, 0));
        assertTrue(teleport.callEvent());
        player.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        assertFalse(new CustomMobMoveEntityEvent(player, CustomMobMoveEntityEvent.Kind.CARRY, null).callEvent());
        assertFalse(new CustomMobMoveEntityEvent(
                        player,
                        CustomMobMoveEntityEvent.Kind.TELEPORT,
                        world.getSpawnLocation().add(0, 20, 0))
                .callEvent());
    }

    @Test
    void arcaneVisionKeepsBlindnessAway() {
        PotionEffect blind = new PotionEffect(PotionEffectType.BLINDNESS, 100, 0);
        assertTrue(blinding(blind).callEvent());
        player.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        assertFalse(blinding(blind).callEvent());
        assertTrue(blinding(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0)).callEvent());
    }

    private EntityPotionEffectEvent blinding(PotionEffect effect) {
        return new EntityPotionEffectEvent(
                player,
                null,
                effect,
                zombie,
                EntityPotionEffectEvent.Cause.ATTACK,
                EntityPotionEffectEvent.Action.ADDED,
                false);
    }
}
