/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.enchantments;

import de.eldoria.bloodnight.api.event.mob.CustomMobMoveEntityEvent;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.BiConsumer;
import java.util.function.ToIntBiFunction;

/**
 * What the {@link BloodEnchantments} do, only during a blood night in the player's world.
 * <p>
 * Night Ward takes {@value #WARD_PER_LEVEL} of the damage a monster deals per level, summed over the worn armor, so
 * four pieces of Night Ward V take away 50%. It applies after the world's damage modifier. Bloodthirst heals
 * {@value #BLOODTHIRST_HEAL_PER_LEVEL} health (one heart) per level for every kill with the weapon in the main hand, on
 * the killer's thread.
 * <p>
 * Dawnbringer adds {@value #DAWNBRINGER_PER_LEVEL} of the damage per level to melee hits on special mobs, at any time
 * of day. Hunter's Mark makes whatever an arrow from its bow or crossbow hits glow for {@value #MARK_SECONDS_PER_LEVEL}
 * seconds per level.
 * <p>
 * Earth Bound and Arcane Vision work at any time: special mobs cannot teleport or carry away a player whose boots have
 * Earth Bound, and a player whose helmet has Arcane Vision is never blinded.
 */
public final class EnchantmentEffects implements Listener {
    static final double WARD_PER_LEVEL = 0.025;
    static final double BLOODTHIRST_HEAL_PER_LEVEL = 2;
    static final double DAWNBRINGER_PER_LEVEL = 0.1;
    static final int MARK_SECONDS_PER_LEVEL = 4;
    private final BloodNights nights;
    private final ToIntBiFunction<ItemStack, NamespacedKey> levels;
    private final BiConsumer<Entity, Runnable> onThread;

    /**
     * @param nights   the running blood nights
     * @param levels   an item's level of an enchantment, e.g. {@link BloodEnchantments#level}
     * @param onThread runs work on an entity's thread, right away when the current thread owns it
     */
    public EnchantmentEffects(
            BloodNights nights,
            ToIntBiFunction<ItemStack, NamespacedKey> levels,
            BiConsumer<Entity, Runnable> onThread) {
        this.nights = nights;
        this.levels = levels;
        this.onThread = onThread;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Entity attacker = event.getDamageSource().getCausingEntity();
        if (!(attacker instanceof Enemy) || nights.running(player.getWorld()).isEmpty()) return;
        int ward = 0;
        for (EquipmentSlot slot : BloodEnchantments.ARMOR) {
            ward += levels.applyAsInt(player.getInventory().getItem(slot), BloodEnchantments.NIGHT_WARD);
        }
        event.setDamage(event.getDamage() * (1 - warded(ward)));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStrike(EntityDamageByEntityEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player player)) return;
        if (event.getDamageSource().getDirectEntity() != player || !MobTags.isCustomMob(event.getEntity())) return;
        int level = levels.applyAsInt(player.getInventory().getItemInMainHand(), BloodEnchantments.DAWNBRINGER);
        event.setDamage(event.getDamage() * (1 + level * DAWNBRINGER_PER_LEVEL));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AbstractArrow arrow) || !(arrow.getShooter() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)
                || nights.running(target.getWorld()).isEmpty()) return;
        int level = levels.applyAsInt(arrow.getWeapon(), BloodEnchantments.HUNTERS_MARK);
        if (level <= 0) return;
        target.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING, level * MARK_SECONDS_PER_LEVEL * 20, 0, false, false, false));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(CustomMobMoveEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (levels.applyAsInt(player.getInventory().getItem(EquipmentSlot.FEET), BloodEnchantments.EARTH_BOUND) > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlind(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PotionEffect added = event.getNewEffect();
        if (added == null || !added.getType().equals(PotionEffectType.BLINDNESS)) return;
        if (levels.applyAsInt(player.getInventory().getItem(EquipmentSlot.HEAD), BloodEnchantments.ARCANE_VISION) > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || nights.running(victim.getWorld()).isEmpty()) return;
        onThread.accept(killer, () -> quench(killer));
    }

    private void quench(Player killer) {
        if (killer.isDead()) return;
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        int level = levels.applyAsInt(weapon, BloodEnchantments.BLOODTHIRST);
        if (level <= 0) return;
        AttributeInstance max = killer.getAttribute(Attribute.MAX_HEALTH);
        double cap = max == null ? killer.getHealth() : max.getValue();
        killer.setHealth(Math.min(cap, killer.getHealth() + level * BLOODTHIRST_HEAL_PER_LEVEL));
    }

    /**
     * @param levels the Night Ward levels of all worn pieces together
     * @return the share of a monster's damage they take away, 0 to 1
     */
    static double warded(int levels) {
        return Math.clamp(levels * WARD_PER_LEVEL, 0, 1);
    }
}
