/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.google.common.base.Function;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

import java.util.EnumMap;
import java.util.Map;

/**
 * Damage events for tests, as the server fires them. Paper builds them internally and offers no constructor without
 * the deprecated damage modifiers; the ones used here are the only ones not marked for removal, so the deprecation is
 * accepted in this one place.
 */
@SuppressWarnings("deprecation")
public final class DamageEvents {
    private DamageEvents() {}

    /**
     * @return an entity hurting another, with a generic damage source
     */
    public static EntityDamageByEntityEvent byEntity(Entity damager, Entity victim, DamageCause cause, double amount) {
        return byEntity(
                damager, victim, cause, DamageSource.builder(DamageType.GENERIC).build(), amount);
    }

    /**
     * @return an entity hurting another
     */
    public static EntityDamageByEntityEvent byEntity(
            Entity damager, Entity victim, DamageCause cause, DamageSource source, double amount) {
        return new EntityDamageByEntityEvent(damager, victim, cause, source, modifiers(amount), functions(), false);
    }

    /**
     * @return an entity hurt by something other than an entity, with a generic damage source
     */
    public static EntityDamageEvent of(Entity victim, DamageCause cause, double amount) {
        return of(victim, cause, DamageSource.builder(DamageType.GENERIC).build(), amount);
    }

    /**
     * @return an entity hurt by something other than an entity
     */
    public static EntityDamageEvent of(Entity victim, DamageCause cause, DamageSource source, double amount) {
        return new EntityDamageEvent(victim, cause, source, modifiers(amount), functions());
    }

    private static Map<DamageModifier, Double> modifiers(double amount) {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, amount);
        return modifiers;
    }

    private static Map<DamageModifier, Function<? super Double, Double>> functions() {
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);
        functions.put(DamageModifier.BASE, ignored -> -0.0);
        return functions;
    }
}
