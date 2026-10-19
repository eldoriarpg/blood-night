/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Keeps the coordinator in step with the world: mobs leave it when they die or unload, and are taken over again when
 * a chunk with tagged mobs loads.
 */
public final class MobLifecycle implements Listener {
    private final MobCoordinator coordinator;

    public MobLifecycle(MobCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        if (event.isCancelled()) return;
        coordinator.died(event.getEntity());
        coordinator.remove(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRemove(EntityRemoveFromWorldEvent event) {
        coordinator.remove(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdd(EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof LivingEntity living) coordinator.attach(living);
    }
}
