/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living.combat;

import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Keeps a custom mob and its extension one creature: damage to the extension goes to the mob, the extension's health
 * follows the mob's, and the extension dies with the mob.
 * <p>
 * Mob and extension ride each other, so they are in the same region. Should one of them be owned by another thread
 * anyway, it is left alone for that event.
 */
public class DamageHandler implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExtensionDamage(EntityDamageEvent event) {
        if (MobTags.isExtension(event.getEntity())) handleExtensionDamage(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBaseDamage(EntityDamageEvent event) {
        if (MobTags.isExtended(event.getEntity())) handleBaseDamage(event);
    }

    /**
     * Damage to an extension is dealt to its base instead, by the same damager, and the extension's health follows the
     * base's.
     */
    private void handleExtensionDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Optional<UUID> baseId = MobTags.getBase(entity);
        if (baseId.isEmpty()) return;
        if (!(entity instanceof LivingEntity extension)) return;
        if (!(entity.getServer().getEntity(baseId.get()) instanceof LivingEntity base) || base.isDead()) return;
        if (!Bukkit.isOwnedByCurrentRegion(base)) return;

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            base.damage(event.getDamage(), byEntity.getDamager());
        } else {
            base.damage(event.getDamage());
        }
        event.setCancelled(true);
        syncHealth(extension, base.getHealth());
        extension.playHurtAnimation(0);
    }

    /**
     * The extension's health follows the base's. Runs at {@code MONITOR}, where the final damage no longer changes.
     */
    private void handleBaseDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Optional<UUID> extensionId = MobTags.getExtension(entity);
        if (!(entity instanceof LivingEntity base)) return;

        if (extensionId.isEmpty()) return;
        if (!(entity.getServer().getEntity(extensionId.get()) instanceof LivingEntity extension)) return;
        if (!Bukkit.isOwnedByCurrentRegion(extension)) return;

        syncHealth(extension, base.getHealth() - event.getFinalDamage());
        extension.playHurtAnimation(0);
    }

    /**
     * Sets the health of an extension, kept within what it can have: base and extension may differ in maximum health.
     * A dead extension is left alone: setting its health would fire its death again, and with it the base's reaction.
     */
    private static void syncHealth(LivingEntity extension, double health) {
        if (extension.isDead()) return;
        AttributeInstance max = extension.getAttribute(Attribute.MAX_HEALTH);
        double limit = max == null ? extension.getHealth() : max.getValue();
        extension.setHealth(Math.clamp(health, 0, limit));
    }

    /**
     * The extension dies with its base.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        var entity = event.getEntity();
        if (!MobTags.isCustomMob(entity)) return;
        Optional<UUID> extensionId = MobTags.getExtension(entity);
        if (extensionId.isEmpty()) return;
        if (entity.getServer().getEntity(extensionId.get()) instanceof LivingEntity extension
                && Bukkit.isOwnedByCurrentRegion(extension)) {
            extension.damage(extension.getHealth());
        }
    }
}
