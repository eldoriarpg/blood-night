/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules.explosion;

import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Keeps explosions of entities marked {@link BukkitRuntime#HARMLESS}, such as TNT a mob lit, from breaking blocks.
 */
public final class HarmlessExplosions implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(BukkitRuntime.HARMLESS, PersistentDataType.BOOLEAN)) {
            event.blockList().clear();
        }
    }
}
