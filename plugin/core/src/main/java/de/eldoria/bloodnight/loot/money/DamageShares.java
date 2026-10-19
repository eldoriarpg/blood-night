/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.money;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The damage each player dealt to the mobs they hit, so the money of a kill can be split by it. A hit counts with its
 * final damage, capped at the health the mob had left, so an overkill does not count; projectiles count for whoever
 * shot them. A mob's count is dropped when it is taken at its death or when the mob leaves the world.
 */
public final class DamageShares implements Listener {
    private final Map<UUID, Map<UUID, Double>> dealt = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob) || mob instanceof Player) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player player)) return;
        double counted = Math.min(event.getFinalDamage(), mob.getHealth());
        if (counted <= 0) return;
        dealt.computeIfAbsent(mob.getUniqueId(), id -> new ConcurrentHashMap<>())
                .merge(player.getUniqueId(), counted, Double::sum);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRemove(EntityRemoveFromWorldEvent event) {
        dealt.remove(event.getEntity().getUniqueId());
    }

    /**
     * @param mob a mob that died
     * @return the damage each player dealt it, by player id, and forgets it
     */
    public Map<UUID, Double> take(LivingEntity mob) {
        Map<UUID, Double> taken = dealt.remove(mob.getUniqueId());
        return taken == null ? Map.of() : Map.copyOf(taken);
    }
}
