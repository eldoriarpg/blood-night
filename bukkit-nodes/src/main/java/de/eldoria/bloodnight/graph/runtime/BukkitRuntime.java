/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * Helpers generated code calls for nodes that need more than one expression.
 */
public final class BukkitRuntime {
    private BukkitRuntime() {}

    /**
     * Launches a projectile from a shooter straight at a target.
     *
     * @param shooter the launching entity
     * @param target  what to aim at
     * @param kind    the projectile
     * @param speed   blocks per tick
     * @return the projectile
     */
    public static Projectile launchAt(LivingEntity shooter, Entity target, ProjectileKind kind, double speed) {
        Vector direction = target.getLocation()
                .toVector()
                .subtract(shooter.getEyeLocation().toVector());
        if (direction.lengthSquared() == 0) direction = shooter.getLocation().getDirection();
        return shooter.launchProjectile(kind.type(), direction.normalize().multiply(speed));
    }

    /**
     * Spawns a lingering cloud applying a potion to everything inside.
     *
     * @param location      centre of the cloud
     * @param potion        the potion applied
     * @param seconds       how long the cloud stays
     * @param radius        starting radius in blocks
     * @param radiusPerTick growth per tick; negative values shrink the cloud
     * @param color         particle colour, or {@code null} for the potion's
     */
    public static void potionCloud(
            Location location,
            PotionType potion,
            int seconds,
            double radius,
            double radiusPerTick,
            @Nullable Color color) {
        location.getWorld().spawn(location, AreaEffectCloud.class, cloud -> {
            cloud.setBasePotionType(potion);
            cloud.setDuration(seconds * 20);
            cloud.setRadius((float) radius);
            cloud.setRadiusPerTick((float) radiusPerTick);
            if (color != null) cloud.setColor(color);
        });
    }

    /**
     * @param location     where to search from
     * @param radius       search radius in blocks
     * @param survivalOnly whether players in other game modes are ignored
     * @return the closest player, or {@code null} when none is in range
     */
    public static @Nullable Player nearestPlayer(Location location, double radius, boolean survivalOnly) {
        return location.getWorld().getNearbyPlayers(location, radius).stream()
                .filter(player -> !survivalOnly || player.getGameMode() == GameMode.SURVIVAL)
                .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(location)))
                .orElse(null);
    }

    /**
     * The entity behind damage: what caused it, such as the player who shot an arrow, the igniter of TNT or the
     * owner of a potion cloud; otherwise the damaging entity itself.
     *
     * @param event a damage event
     * @return the attacker, or {@code null} when no entity is involved
     */
    public static @Nullable Entity attacker(EntityDamageEvent event) {
        Entity causing = event.getDamageSource().getCausingEntity();
        if (causing != null) return causing;
        return event instanceof EntityDamageByEntityEvent byEntity ? byEntity.getDamager() : null;
    }

    /**
     * @param event a death event
     * @return the entity that caused the death, or {@code null} when none did
     */
    public static @Nullable Entity attacker(EntityDeathEvent event) {
        return event.getDamageSource().getCausingEntity();
    }

    /**
     * @param event a death event
     * @return the entity that caused the death unless it is the victim itself, or {@code null}
     */
    public static @Nullable Entity killer(EntityDeathEvent event) {
        Entity killer = attacker(event);
        return killer == event.getEntity() ? null : killer;
    }
}
