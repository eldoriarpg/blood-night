/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.advancements;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import de.eldoria.bloodnight.api.event.night.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.night.BloodNightEndEvent;
import de.eldoria.bloodnight.enchantments.BloodEnchantments;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;

/**
 * Awards the advancements of the plugin's data pack, the "Blood Night" tab. Every criterion is
 * {@code minecraft:impossible}, so only this class grants them, on the player's thread, while {@code advancements} is
 * on in {@code config.yml}. Any award also grants the tab's root, so the tab shows up with the first one.
 * <ul>
 *     <li>{@code survive}: being alive in the world when a blood night ends</li>
 *     <li>{@code slayer}: killing {@value #SLAYER_KILLS} special mobs during one blood night</li>
 *     <li>{@code collector}: killing every mob of the built-in store once; one criterion per mob id</li>
 *     <li>{@code blood_dragon}: being within {@value #DRAGON_REACH} blocks when the Blood Dragon dies</li>
 *     <li>{@code night_ward}: putting on armor with Night Ward V</li>
 * </ul>
 */
public final class BloodAdvancements implements Listener {
    static final NamespacedKey ROOT = key("root");
    static final NamespacedKey SURVIVE = key("survive");
    static final NamespacedKey SLAYER = key("slayer");
    static final NamespacedKey COLLECTOR = key("collector");
    static final NamespacedKey BLOOD_DRAGON = key("blood_dragon");
    static final NamespacedKey NIGHT_WARD = key("night_ward");
    static final String GRANTED = "granted";
    static final int SLAYER_KILLS = 10;
    static final double DRAGON_REACH = 192;
    static final int NIGHT_WARD_LEVEL = 5;
    private final BloodNights nights;
    private final BooleanSupplier enabled;
    private final Set<String> storeMobs;
    private final String bloodDragon;
    private final Grants grants;
    private final ToIntBiFunction<ItemStack, NamespacedKey> levels;
    private final Map<UUID, Map<UUID, Integer>> kills = new ConcurrentHashMap<>();

    /**
     * @param nights       the running blood nights
     * @param enabled      whether advancements are on
     * @param storeMobs    the ids of the built-in store's mobs
     * @param bloodDragon  the id of the Blood Dragon
     * @param grants       grants a criterion of an advancement, e.g. {@link #progress}
     * @param levels       an item's level of an enchantment, e.g. {@link BloodEnchantments#level}
     */
    public BloodAdvancements(
            BloodNights nights,
            BooleanSupplier enabled,
            Set<String> storeMobs,
            String bloodDragon,
            Grants grants,
            ToIntBiFunction<ItemStack, NamespacedKey> levels) {
        this.nights = nights;
        this.enabled = enabled;
        this.storeMobs = Set.copyOf(storeMobs);
        this.bloodDragon = bloodDragon;
        this.grants = grants;
        this.levels = levels;
    }

    /**
     * @param advancements the advancement of a key, if the data pack is loaded
     * @param onThread     runs work on an entity's thread, right away when the current thread owns it
     * @return grants criteria on the player's own advancement progress, on the player's thread
     */
    public static Grants progress(
            Function<NamespacedKey, @Nullable Advancement> advancements, BiConsumer<Entity, Runnable> onThread) {
        return (player, key, criterion) -> {
            Advancement advancement = advancements.apply(key);
            if (advancement == null) return;
            onThread.accept(
                    player, () -> player.getAdvancementProgress(advancement).awardCriteria(criterion));
        };
    }

    private static NamespacedKey key(String name) {
        return new NamespacedKey("bloodnight", "blood_night/" + name);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        kills.remove(event.getWorld().getUID());
        for (Player player : event.getWorld().getPlayers()) award(player, ROOT, GRANTED);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        World world = event.getWorld();
        kills.remove(world.getUID());
        for (Player player : world.getPlayers()) {
            if (!player.isDead()) award(player, SURVIVE, GRANTED);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity mob = event.getEntity();
        Optional<String> id = MobTags.getCustomMobId(mob);
        if (id.isEmpty()) return;
        if (id.get().equals(bloodDragon)) slain(mob);
        Player killer = mob.getKiller();
        if (killer == null) return;
        if (storeMobs.contains(id.get())) award(killer, COLLECTOR, id.get());
        if (nights.running(mob.getWorld()).isEmpty()) return;
        int count = kills.computeIfAbsent(mob.getWorld().getUID(), ignored -> new ConcurrentHashMap<>())
                .merge(killer.getUniqueId(), 1, Integer::sum);
        if (count == SLAYER_KILLS) award(killer, SLAYER, GRANTED);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmor(PlayerArmorChangeEvent event) {
        if (levels.applyAsInt(event.getNewItem(), BloodEnchantments.NIGHT_WARD) >= NIGHT_WARD_LEVEL) {
            award(event.getPlayer(), NIGHT_WARD, GRANTED);
        }
    }

    private void slain(LivingEntity dragon) {
        double reach = DRAGON_REACH * DRAGON_REACH;
        for (Player player : dragon.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(dragon.getLocation()) <= reach) {
                award(player, BLOOD_DRAGON, GRANTED);
            }
        }
    }

    private void award(Player player, NamespacedKey key, String criterion) {
        if (!enabled.getAsBoolean()) return;
        grants.grant(player, ROOT, GRANTED);
        if (!key.equals(ROOT)) grants.grant(player, key, criterion);
    }

    /**
     * Grants one criterion of an advancement to a player.
     */
    @FunctionalInterface
    public interface Grants {
        void grant(Player player, NamespacedKey advancement, String criterion);
    }
}
