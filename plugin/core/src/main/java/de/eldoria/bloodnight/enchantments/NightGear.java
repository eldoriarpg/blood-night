/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.enchantments;

import de.eldoria.bloodnight.night.BloodNights;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.ToIntBiFunction;

/**
 * The {@link BloodEnchantments} worn on the body, which last as long as a blood night runs in the player's world.
 * <p>
 * Blood Walker boots make the player {@value #WALKER_PER_LEVEL} faster per level, through a movement speed modifier
 * that is never saved. A Blood Vision helmet gives night vision that only the player's client knows of: no icon, no
 * particles, and a real night vision potion stays untouched. Minecraft draws night vision at one strength only. The
 * Curse of the Blood Moon on any armor piece turns monsters within {@value #LURE} blocks that hunt no player towards
 * a wearer in survival or adventure mode.
 * {@link #tick()} runs every {@value #STEP} ticks on the global region and hands each player to their own thread; it
 * sends the night vision again each time, so it survives respawns and world changes.
 */
public final class NightGear implements Listener {
    /**
     * Ticks between two steps of {@link #tick()}.
     */
    public static final int STEP = 40;

    static final double WALKER_PER_LEVEL = 0.1;
    static final int LURE = 16;
    private static final int VISION_TICKS = STEP * 10;
    private final Server server;
    private final BloodNights nights;
    private final ToIntBiFunction<ItemStack, NamespacedKey> levels;
    private final BiConsumer<Entity, Runnable> onThread;
    private final Set<UUID> seeing = ConcurrentHashMap.newKeySet();

    /**
     * @param server   the server
     * @param nights   the running blood nights
     * @param levels   an item's level of an enchantment, e.g. {@link BloodEnchantments#level}
     * @param onThread runs work on an entity's thread, right away when the current thread owns it
     */
    public NightGear(
            Server server,
            BloodNights nights,
            ToIntBiFunction<ItemStack, NamespacedKey> levels,
            BiConsumer<Entity, Runnable> onThread) {
        this.server = server;
        this.nights = nights;
        this.levels = levels;
        this.onThread = onThread;
    }

    /**
     * Gives every player what their gear grants right now.
     */
    public void tick() {
        for (Player player : server.getOnlinePlayers()) onThread.accept(player, () -> wear(player));
    }

    /**
     * Takes the gear's effects from every player, e.g. when the plugin disables.
     */
    public void clear() {
        for (Player player : server.getOnlinePlayers()) {
            onThread.accept(player, () -> {
                walk(player, 0);
                see(player, false);
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        seeing.remove(event.getPlayer().getUniqueId());
    }

    void wear(Player player) {
        boolean night = nights.running(player.getWorld()).isPresent();
        var armor = player.getInventory();
        walk(player, night ? levels.applyAsInt(armor.getItem(EquipmentSlot.FEET), BloodEnchantments.BLOOD_WALKER) : 0);
        see(player, night && levels.applyAsInt(armor.getItem(EquipmentSlot.HEAD), BloodEnchantments.BLOOD_VISION) > 0);
        if (night && cursed(player)) lure(player);
    }

    private boolean cursed(Player player) {
        for (EquipmentSlot slot : BloodEnchantments.ARMOR) {
            if (levels.applyAsInt(player.getInventory().getItem(slot), BloodEnchantments.BLOOD_MOON_CURSE) > 0) {
                return true;
            }
        }
        return false;
    }

    private static void lure(Player player) {
        GameMode mode = player.getGameMode();
        if (mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) return;
        for (Entity entity : player.getNearbyEntities(LURE, LURE / 2.0, LURE)) {
            if (!(entity instanceof Mob mob) || !(entity instanceof Enemy)) continue;
            if (mob.getTarget() instanceof Player || !Bukkit.isOwnedByCurrentRegion(mob)) continue;
            mob.setTarget(player);
        }
    }

    private static void walk(Player player, int level) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) return;
        double bonus = level * WALKER_PER_LEVEL;
        AttributeModifier current = speed.getModifier(BloodEnchantments.BLOOD_WALKER);
        if (current != null && current.getAmount() == bonus) return;
        if (current != null) speed.removeModifier(BloodEnchantments.BLOOD_WALKER);
        if (level <= 0) return;
        speed.addTransientModifier(new AttributeModifier(
                BloodEnchantments.BLOOD_WALKER,
                bonus,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                EquipmentSlotGroup.ANY));
    }

    private void see(Player player, boolean vision) {
        if (vision) {
            seeing.add(player.getUniqueId());
            player.sendPotionEffectChange(
                    player, new PotionEffect(PotionEffectType.NIGHT_VISION, VISION_TICKS, 0, false, false, false));
            return;
        }
        if (!seeing.remove(player.getUniqueId())) return;
        PotionEffect own = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        if (own == null) player.sendPotionEffectChangeRemove(player, PotionEffectType.NIGHT_VISION);
        else player.sendPotionEffectChange(player, own);
    }
}
