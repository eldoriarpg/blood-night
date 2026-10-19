/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living.movement;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import de.eldoria.bloodnight.api.internal.MovementPreset;
import de.eldoria.bloodnight.mob.attributes.Movement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Steers mobs through Paper's goal API: a mob gets one {@link MovementGoal} at the highest priority, replaced when its
 * movement changes and dropped for {@code VANILLA}. {@code AGGRESSIVE} also raises the mob's follow range to its
 * range, which stays when the movement changes again. Goals are not saved with the entity, so the coordinator steers
 * a mob again whenever it takes it over.
 */
public final class PaperSteering implements Steering, Listener {
    private final MobGoals goals;
    private final Map<UUID, MovementGoal> steered = new ConcurrentHashMap<>();

    /**
     * @param goals the server's mob goals
     */
    public PaperSteering(MobGoals goals) {
        this.goals = goals;
    }

    @Override
    public void steer(LivingEntity entity, Movement movement) {
        if (!(entity instanceof Mob mob)) return;
        goals.removeGoal(mob, MovementGoal.KEY);
        steered.remove(mob.getUniqueId());
        if (movement.preset() == MovementPreset.VANILLA) return;
        if (movement.preset() == MovementPreset.AGGRESSIVE) followTo(mob, movement.range());
        MovementGoal goal = new MovementGoal(mob, movement);
        goals.addGoal(mob, 0, goal);
        steered.put(mob.getUniqueId(), goal);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        MovementGoal goal = steered.get(event.getDamager().getUniqueId());
        if (goal != null) goal.hit();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRemove(EntityRemoveFromWorldEvent event) {
        steered.remove(event.getEntity().getUniqueId());
    }

    private static void followTo(Mob mob, double range) {
        AttributeInstance follow = mob.getAttribute(Attribute.FOLLOW_RANGE);
        if (follow != null) follow.setBaseValue(range);
    }
}
