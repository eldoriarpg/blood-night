/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The event triggers of blood-night.
 * <p>
 * Damage is split as before: {@code event.damage} only for damage without an attacking entity,
 * {@code event.damage_by_entity} and {@code event.damage_by_player} for the rest. Kills are damage that will kill:
 * final damage at least the victim's health. {@code mob.tick} and {@code debug.trigger} have no event; the plugin
 * calls them itself.
 */
public final class Triggers {
    public static final List<Trigger<?>> EVENTS = List.of(
            new Trigger<>(
                    "event.damage",
                    "On Damage",
                    "The mob took damage from something other than an entity",
                    EntityDamageEvent.class,
                    "onDamage",
                    EntityDamageEvent::getEntity,
                    event -> !(event instanceof EntityDamageByEntityEvent),
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onDamage),
            new Trigger<>(
                    "event.damage_by_entity",
                    "On Damage By Entity",
                    "The mob took damage from an entity that is not a player",
                    EntityDamageByEntityEvent.class,
                    "onDamageByEntity",
                    EntityDamageByEntityEvent::getEntity,
                    event -> !(event.getDamager() instanceof Player),
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onDamageByEntity),
            new Trigger<>(
                    "event.damage_by_player",
                    "On Damage By Player",
                    "The mob took damage from a player",
                    EntityDamageByEntityEvent.class,
                    "onDamageByPlayer",
                    EntityDamageByEntityEvent::getEntity,
                    event -> event.getDamager() instanceof Player,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onDamageByPlayer),
            new Trigger<>(
                    "event.hit",
                    "On Hit",
                    "The mob damaged another entity",
                    EntityDamageByEntityEvent.class,
                    "onHit",
                    EntityDamageByEntityEvent::getDamager,
                    event -> true,
                    EventPriority.NORMAL,
                    false,
                    CompiledBehaviour::onHit),
            new Trigger<>(
                    "event.kill",
                    "On Kill",
                    "The mob is about to kill an entity or player",
                    EntityDamageByEntityEvent.class,
                    "onKill",
                    EntityDamageByEntityEvent::getDamager,
                    Triggers::kills,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onKill),
            new Trigger<>(
                    "event.player_kill",
                    "On Player Kill",
                    "The mob is about to kill a player",
                    EntityDamageByEntityEvent.class,
                    "onPlayerKill",
                    EntityDamageByEntityEvent::getDamager,
                    event -> kills(event) && event.getEntity() instanceof Player,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onPlayerKill),
            new Trigger<>(
                    "event.entity_kill",
                    "On Entity Kill",
                    "The mob is about to kill an entity that is not a player",
                    EntityDamageByEntityEvent.class,
                    "onEntityKill",
                    EntityDamageByEntityEvent::getDamager,
                    event -> kills(event) && !(event.getEntity() instanceof Player),
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onEntityKill),
            new Trigger<>(
                    "event.death",
                    "On Death",
                    "The mob died",
                    EntityDeathEvent.class,
                    "onDeath",
                    EntityDeathEvent::getEntity,
                    event -> true,
                    EventPriority.HIGH,
                    false,
                    CompiledBehaviour::onDeath),
            new Trigger<>(
                    "event.teleport",
                    "On Teleport",
                    "The mob teleports",
                    EntityTeleportEvent.class,
                    "onTeleport",
                    EntityTeleportEvent::getEntity,
                    event -> true,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onTeleport),
            new Trigger<>(
                    "event.target_change",
                    "On Target Change",
                    "The mob gets or loses a target",
                    EntityTargetLivingEntityEvent.class,
                    "onTargetChange",
                    EntityTargetLivingEntityEvent::getEntity,
                    event -> true,
                    EventPriority.NORMAL,
                    true,
                    CompiledBehaviour::onTargetChange),
            new Trigger<>(
                    "event.projectile_shoot",
                    "On Projectile Shoot",
                    "The mob launched a projectile",
                    ProjectileLaunchEvent.class,
                    "onProjectileShoot",
                    event -> shooter(event.getEntity()),
                    event -> true,
                    EventPriority.NORMAL,
                    false,
                    CompiledBehaviour::onProjectileShoot),
            new Trigger<>(
                    "event.projectile_hit",
                    "On Projectile Hit",
                    "A projectile launched by the mob hit something",
                    ProjectileHitEvent.class,
                    "onProjectileHit",
                    event -> shooter(event.getEntity()),
                    event -> true,
                    EventPriority.NORMAL,
                    false,
                    CompiledBehaviour::onProjectileHit),
            new Trigger<>(
                    "event.explosion_prime",
                    "On Explosion Prime",
                    "The mob is about to explode",
                    ExplosionPrimeEvent.class,
                    "onExplosionPrime",
                    ExplosionPrimeEvent::getEntity,
                    event -> true,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onExplosionPrime),
            new Trigger<>(
                    "event.explode",
                    "On Explode",
                    "The mob explodes",
                    EntityExplodeEvent.class,
                    "onExplode",
                    EntityExplodeEvent::getEntity,
                    event -> true,
                    EventPriority.NORMAL,
                    false,
                    CompiledBehaviour::onExplode));

    private Triggers() {}

    private static boolean kills(EntityDamageByEntityEvent event) {
        return event.getEntity() instanceof LivingEntity living && event.getFinalDamage() >= living.getHealth();
    }

    private static @Nullable Entity shooter(Projectile projectile) {
        return projectile.getShooter() instanceof Entity entity ? entity : null;
    }
}
