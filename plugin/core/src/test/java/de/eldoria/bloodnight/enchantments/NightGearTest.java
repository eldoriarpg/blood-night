/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.enchantments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightTimes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.Nullable;
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
import static org.junit.jupiter.api.Assertions.assertNull;

class NightGearTest {
    private World world;
    private BloodNights nights;
    private Viewer player;
    private NightGear gear;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
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
        gear = new NightGear(server, nights, NightGearTest::level, (entity, work) -> work.run());
        player = new Viewer(server);
        server.addPlayer(player);
        player.teleport(world.getSpawnLocation());
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static int level(ItemStack item, NamespacedKey enchantment) {
        if (enchantment.equals(BloodEnchantments.BLOOD_MOON_CURSE))
            return item.getType() == Material.LEATHER_LEGGINGS ? 1 : 0;
        if (enchantment.equals(BloodEnchantments.BLOOD_WALKER) && item.getType() == Material.DIAMOND_BOOTS) return 2;
        return enchantment.equals(BloodEnchantments.BLOOD_VISION) && item.getType() == Material.DIAMOND_HELMET ? 1 : 0;
    }

    private void at(long time) {
        world.setFullTime(time);
        nights.check(List.of(world));
    }

    private double speedBonus() {
        var modifier = player.getAttribute(Attribute.MOVEMENT_SPEED).getModifier(BloodEnchantments.BLOOD_WALKER);
        return modifier == null ? 0 : modifier.getAmount();
    }

    @Test
    void bloodWalkersWalkFasterOnlyDuringBloodNights() {
        gear.tick();
        assertEquals(0, speedBonus(), 1e-9);

        at(13_000);
        gear.tick();
        gear.tick();
        assertEquals(2 * NightGear.WALKER_PER_LEVEL, speedBonus(), 1e-9);

        at(24_000);
        gear.tick();
        assertEquals(0, speedBonus(), 1e-9);
    }

    @Test
    void bloodVisionSeesInTheDarkOnlyDuringBloodNights() {
        gear.tick();
        assertNull(player.vision);

        at(13_000);
        gear.tick();
        assertEquals(PotionEffectType.NIGHT_VISION, player.vision.getType());
        assertFalse(player.vision.hasIcon());

        at(24_000);
        gear.tick();
        assertNull(player.vision);
    }

    @Test
    void anOwnNightVisionPotionStaysVisible() {
        at(13_000);
        gear.tick();
        PotionEffect potion = new PotionEffect(PotionEffectType.NIGHT_VISION, 3600, 0);
        player.addPotionEffect(potion);

        at(24_000);
        gear.tick();
        assertEquals(potion, player.vision);
    }

    @Test
    void theCurseDrawsIdleMonstersDuringBloodNights() {
        Zombie zombie = world.spawn(player.getLocation().add(4, 0, 0), Zombie.class);
        player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        gear.tick();
        assertNull(zombie.getTarget());

        at(13_000);
        gear.tick();
        assertEquals(player, zombie.getTarget());
    }

    @Test
    void withoutTheCurseMonstersAreLeftAlone() {
        Zombie zombie = world.spawn(player.getLocation().add(4, 0, 0), Zombie.class);
        at(13_000);
        gear.tick();
        assertNull(zombie.getTarget());
    }

    @Test
    void takingTheBootsOffEndsTheSpeed() {
        at(13_000);
        gear.tick();
        player.getInventory().setBoots(null);
        gear.tick();
        assertEquals(0, speedBonus(), 1e-9);
    }

    @Test
    void clearingTakesEverythingBack() {
        at(13_000);
        gear.tick();
        gear.clear();
        assertEquals(0, speedBonus(), 1e-9);
    }

    /**
     * A player that remembers the night vision its client was last told of, which MockBukkit does not track.
     */
    private static final class Viewer extends PlayerMock {
        private @Nullable PotionEffect vision;

        private Viewer(ServerMock server) {
            super(server, "viewer");
        }

        @Override
        public void sendPotionEffectChange(LivingEntity entity, PotionEffect effect) {
            if (effect.getType().equals(PotionEffectType.NIGHT_VISION)) vision = effect;
        }

        @Override
        public void sendPotionEffectChangeRemove(LivingEntity entity, PotionEffectType type) {
            if (type.equals(PotionEffectType.NIGHT_VISION)) vision = null;
        }
    }
}
