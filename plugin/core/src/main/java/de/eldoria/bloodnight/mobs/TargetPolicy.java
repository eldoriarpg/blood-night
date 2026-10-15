/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/**
 * Custom mobs only target players, and are never targeted by other custom mobs; otherwise they would kill each
 * other. Runs before the target-change trigger, which skips cancelled events.
 */
public final class TargetPolicy implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        LivingEntity target = event.getTarget();
        if (target == null || !MobTags.isCustomMob(event.getEntity())) return;
        if (MobTags.isCustomMob(target) || !(target instanceof Player)) event.setCancelled(true);
    }
}
