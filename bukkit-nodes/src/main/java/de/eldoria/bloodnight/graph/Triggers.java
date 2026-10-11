/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import org.bukkit.entity.Entity;
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
import java.util.Set;

/**
 * The event triggers of blood-night.
 * <p>
 * Damage is split as before: {@code event.damage} only for damage without an attacking entity,
 * {@code event.damage_by_entity} and {@code event.damage_by_player} for the rest, split on the attacker: the entity
 * that caused the damage, so a player's arrow counts as the player. Kills are deaths whose damage the mob caused.
 * The extension triggers fire for the entity riding or carrying the mob and run the mob's behaviour.
 * {@code mob.tick} and {@code debug.trigger} have no event; the plugin calls them itself.
 */
public final class Triggers {
    /**
     * Triggers with an {@code attacker} output: the entity that caused the damage or death, see
     * {@link BukkitRuntime#attacker(EntityDamageEvent)}.
     */
    public static final Set<String> WITH_ATTACKER = Set.of(
            BloodNightNodes.EVENT_DAMAGE_BY_ENTITY,
            BloodNightNodes.EVENT_DAMAGE_BY_PLAYER,
            BloodNightNodes.EVENT_HIT,
            BloodNightNodes.EVENT_DEATH,
            BloodNightNodes.EVENT_EXTENSION_DAMAGE);

    public static final List<Trigger<?>> EVENTS = List.of(
            new Trigger<>(
                    BloodNightNodes.EVENT_DAMAGE,
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
                    BloodNightNodes.EVENT_DAMAGE_BY_ENTITY,
                    "On Damage By Entity",
                    "The mob took damage from an entity that is not a player, or from what such an entity shot or"
                            + " set off",
                    EntityDamageByEntityEvent.class,
                    "onDamageByEntity",
                    EntityDamageByEntityEvent::getEntity,
                    event -> !(BukkitRuntime.attacker(event) instanceof Player),
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onDamageByEntity),
            new Trigger<>(
                    BloodNightNodes.EVENT_DAMAGE_BY_PLAYER,
                    "On Damage By Player",
                    "The mob took damage from a player, or from what a player shot or set off",
                    EntityDamageByEntityEvent.class,
                    "onDamageByPlayer",
                    EntityDamageByEntityEvent::getEntity,
                    event -> BukkitRuntime.attacker(event) instanceof Player,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onDamageByPlayer),
            new Trigger<>(
                    BloodNightNodes.EVENT_HIT,
                    "On Hit",
                    "The mob damaged another entity, directly or with what it shot or set off",
                    EntityDamageByEntityEvent.class,
                    "onHit",
                    BukkitRuntime::attacker,
                    event -> true,
                    EventPriority.NORMAL,
                    false,
                    CompiledBehaviour::onHit),
            new Trigger<>(
                    BloodNightNodes.EVENT_KILL,
                    "On Kill",
                    "The mob killed an entity or player",
                    EntityDeathEvent.class,
                    "onKill",
                    BukkitRuntime::killer,
                    event -> true,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onKill),
            new Trigger<>(
                    BloodNightNodes.EVENT_PLAYER_KILL,
                    "On Player Kill",
                    "The mob killed a player",
                    EntityDeathEvent.class,
                    "onPlayerKill",
                    BukkitRuntime::killer,
                    event -> event.getEntity() instanceof Player,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onPlayerKill),
            new Trigger<>(
                    BloodNightNodes.EVENT_ENTITY_KILL,
                    "On Entity Kill",
                    "The mob killed an entity that is not a player",
                    EntityDeathEvent.class,
                    "onEntityKill",
                    BukkitRuntime::killer,
                    event -> !(event.getEntity() instanceof Player),
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onEntityKill),
            new Trigger<>(
                    BloodNightNodes.EVENT_DEATH,
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
                    BloodNightNodes.EVENT_TELEPORT,
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
                    BloodNightNodes.EVENT_TARGET_CHANGE,
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
                    BloodNightNodes.EVENT_PROJECTILE_SHOOT,
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
                    BloodNightNodes.EVENT_PROJECTILE_HIT,
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
                    BloodNightNodes.EVENT_EXPLOSION_PRIME,
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
                    BloodNightNodes.EVENT_EXPLODE,
                    "On Explode",
                    "The mob explodes",
                    EntityExplodeEvent.class,
                    "onExplode",
                    EntityExplodeEvent::getEntity,
                    event -> true,
                    EventPriority.NORMAL,
                    false,
                    CompiledBehaviour::onExplode),
            new Trigger<>(
                    BloodNightNodes.EVENT_EXTENSION_DAMAGE,
                    "On Extension Damage",
                    "The entity riding or carrying the mob took damage; cancelling it also keeps the damage from"
                            + " being passed on to the mob",
                    EntityDamageEvent.class,
                    "onExtensionDamage",
                    EntityDamageEvent::getEntity,
                    event -> true,
                    EventPriority.HIGH,
                    true,
                    CompiledBehaviour::onExtensionDamage,
                    true),
            new Trigger<>(
                    BloodNightNodes.EVENT_EXTENSION_DEATH,
                    "On Extension Death",
                    "The entity riding or carrying the mob died",
                    EntityDeathEvent.class,
                    "onExtensionDeath",
                    EntityDeathEvent::getEntity,
                    event -> true,
                    EventPriority.HIGH,
                    false,
                    CompiledBehaviour::onExtensionDeath,
                    true));

    private Triggers() {}

    private static @Nullable Entity shooter(Projectile projectile) {
        return projectile.getShooter() instanceof Entity entity ? entity : null;
    }
}
