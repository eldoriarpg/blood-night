/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.configuration.elements.world.MobSettings;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.function.Function;

/**
 * Makes blood nights more dangerous: damage enemies deal to players, directly or by projectile, is scaled by the
 * world's damage modifier, and damage players deal to ordinary (not special) enemies is divided by its health
 * multiplier.
 */
public final class MobDamage implements Listener {
    private final BloodNights nights;
    private final Function<World, MobSettings> settings;

    /**
     * @param nights   the running blood nights
     * @param settings the mob settings of a world
     */
    public MobDamage(BloodNights nights, Function<World, MobSettings> settings) {
        this.nights = nights;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        World world = event.getEntity().getWorld();
        if (nights.running(world).isEmpty()) return;
        Entity attacker = event.getDamageSource().getCausingEntity();
        Entity victim = event.getEntity();
        MobSettings mobSettings = settings.apply(world);
        if (victim instanceof Player && attacker instanceof Enemy) {
            event.setDamage(event.getDamage() * mobSettings.damageModifier());
        } else if (attacker instanceof Player && victim instanceof Enemy && isOrdinary(victim)) {
            event.setDamage(event.getDamage() / mobSettings.healthMultiplier());
        }
    }

    private static boolean isOrdinary(Entity entity) {
        return !MobTags.isCustomMob(entity) && !MobTags.isExtension(entity);
    }
}
