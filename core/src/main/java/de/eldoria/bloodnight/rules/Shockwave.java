/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.configuration.elements.world.death.ShockwaveSettings;
import de.eldoria.bloodnight.configuration.elements.world.death.TimedEffect;
import de.eldoria.bloodnight.util.Effects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.random.RandomGenerator;

/**
 * Pushes entities away from a point and gives living ones the shockwave's effects; both fall off with the distance and
 * end at the shockwave's range.
 * <p>
 * Entities are only looked up when the current thread owns the whole area; on Folia a shockwave at a region's edge
 * shows its particles but pushes nobody.
 */
final class Shockwave {
    private static final double LIFT = 0.1;
    private static final double MAX_VERTICAL = 0.2;

    private Shockwave() {}

    static void dispatch(Location center, ShockwaveSettings settings, RandomGenerator random) {
        double range = settings.range();
        for (int i = 0; i < 100; i++) {
            Vector direction = randomDirection(random).multiply(range);
            center.getWorld()
                    .spawnParticle(
                            Particle.EXPLOSION,
                            center,
                            0,
                            direction.getX(),
                            direction.getY(),
                            direction.getZ(),
                            range / 100);
        }
        if (!Bukkit.isOwnedByCurrentRegion(center, (int) Math.ceil(range / 16))) return;
        for (Entity entity : center.getWorld().getNearbyEntities(center, range, range, range)) {
            Vector offset = entity.getLocation().toVector().subtract(center.toVector());
            double share = share(offset, range);
            if (share <= 0) continue;
            Vector push = push(offset, share * settings.power() / 10);
            if (push != null) entity.setVelocity(push);
            if (entity instanceof LivingEntity living) applyEffects(living, settings, share);
        }
    }

    /**
     * @return how strongly the shockwave reaches an entity at the offset: 1 at the center, 0 at the range and beyond
     */
    static double share(Vector offset, double range) {
        double rangeSquared = range * range;
        double distanceSquared = offset.lengthSquared();
        if (distanceSquared >= rangeSquared) return 0;
        return 1 - distanceSquared / rangeSquared;
    }

    /**
     * @return the velocity pushing an entity at the offset away with the strength, slightly lifted and with little
     *     vertical speed; null at the center itself
     */
    static @Nullable Vector push(Vector offset, double strength) {
        if (offset.lengthSquared() == 0) return null;
        Vector push = offset.clone().normalize();
        if (Math.abs(push.getY()) <= LIFT) push.setY(push.getY() + LIFT);
        push.multiply(strength);
        if (!Double.isFinite(push.getX()) || !Double.isFinite(push.getY()) || !Double.isFinite(push.getZ()))
            return null;
        push.setY(Math.clamp(push.getY(), -MAX_VERTICAL, MAX_VERTICAL));
        return push;
    }

    /**
     * @return how many ticks an effect of the given seconds lasts at the share, never below the minimum
     */
    static int ticks(double seconds, double share, double minSeconds) {
        return (int) Math.round(Math.max(minSeconds, seconds * share) * 20);
    }

    private static void applyEffects(LivingEntity living, ShockwaveSettings settings, double share) {
        for (TimedEffect effect : settings.effects()) {
            PotionEffectType type = Effects.effect(effect.effect());
            if (type == null) continue;
            living.addPotionEffect(new PotionEffect(type, ticks(effect.seconds(), share, settings.minSeconds()), 1));
        }
    }

    private static Vector randomDirection(RandomGenerator random) {
        return new Vector(random.nextDouble(-1, 1), random.nextDouble(-1, 1), random.nextDouble(-1, 1)).normalize();
    }
}
