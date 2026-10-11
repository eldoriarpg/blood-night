/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 * Behaviour of a custom mob, generated and compiled at runtime from its node graph.
 * <p>
 * One method per trigger. Every method is a no-op by default; the generated class overrides those its graph uses.
 * Each living mob has its own instance, constructed with its {@link MobContext}. This is the only type the plugin and
 * generated code share besides the context.
 */
public interface CompiledBehaviour {
    /**
     * Called once when the mob has been spawned and outfitted; not when it is taken over again after its chunk loaded.
     */
    default void onSpawn() {}

    /**
     * Called once when the night ends in the mob's world, while the mob is still alive.
     */
    default void onNightEnd() {}

    /**
     * Called once per server tick.
     */
    default void onTick() {}

    /**
     * Called when a debug trigger is fired by command.
     */
    default void onDebug() {}

    /**
     * The mob took damage from something other than an entity.
     */
    default void onDamage(EntityDamageEvent event) {}

    /**
     * The mob took damage from an entity that is not a player.
     */
    default void onDamageByEntity(EntityDamageByEntityEvent event) {}

    /**
     * The mob took damage from a player.
     */
    default void onDamageByPlayer(EntityDamageByEntityEvent event) {}

    /**
     * The mob damaged another entity.
     */
    default void onHit(EntityDamageByEntityEvent event) {}

    /**
     * The mob killed an entity or player: the victim's death, caused by the mob or what it shot or set off.
     */
    default void onKill(EntityDeathEvent event) {}

    /**
     * The mob killed a player.
     */
    default void onPlayerKill(EntityDeathEvent event) {}

    /**
     * The mob killed an entity that is not a player.
     */
    default void onEntityKill(EntityDeathEvent event) {}

    /**
     * The mob died.
     */
    default void onDeath(EntityDeathEvent event) {}

    /**
     * The mob teleports.
     */
    default void onTeleport(EntityTeleportEvent event) {}

    /**
     * The mob gets or loses a target.
     */
    default void onTargetChange(EntityTargetLivingEntityEvent event) {}

    /**
     * The mob launched a projectile.
     */
    default void onProjectileShoot(ProjectileLaunchEvent event) {}

    /**
     * A projectile launched by the mob hit something.
     */
    default void onProjectileHit(ProjectileHitEvent event) {}

    /**
     * The mob is about to explode.
     */
    default void onExplosionPrime(ExplosionPrimeEvent event) {}

    /**
     * The mob explodes.
     */
    default void onExplode(EntityExplodeEvent event) {}

    /**
     * The entity riding or carrying the mob took damage.
     */
    default void onExtensionDamage(EntityDamageEvent event) {}

    /**
     * The entity riding or carrying the mob died.
     */
    default void onExtensionDeath(EntityDeathEvent event) {}
}
