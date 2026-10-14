/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.api.event.CustomMobDeathEvent;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginManager;

/**
 * Tells other plugins when a custom mob dies, with {@link CustomMobDeathEvent}. Runs after the drops were added, so
 * listeners see and can change the final drops.
 */
public final class MobDeaths implements Listener {
    private final PluginManager events;

    /**
     * @param events where the event is called
     */
    public MobDeaths(PluginManager events) {
        this.events = events;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (MobTags.isExtension(entity)) return;
        MobTags.getCustomMobId(entity)
                .ifPresent(id -> events.callEvent(new CustomMobDeathEvent(
                        entity, id, event.getDamageSource().getCausingEntity(), event.getDrops())));
    }
}
