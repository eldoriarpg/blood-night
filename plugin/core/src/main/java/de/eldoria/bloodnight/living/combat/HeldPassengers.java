/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living.combat;

import de.eldoria.bloodnight.graph.runtime.world.Carrying;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps entities a mob carries (see {@link Carrying}) from getting off on their own. Holding sneak does nothing, but a
 * carried player who presses it {@value #BREAK_FREE} times breaks loose. They also get off when the mob drops them,
 * when the mob dies or is gone, and when they die or leave the server; the marks are removed then.
 */
public final class HeldPassengers implements Listener {
    static final int BREAK_FREE = 5;
    private final Map<UUID, Boolean> sneaking = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onDismount(EntityDismountEvent event) {
        Entity passenger = event.getEntity();
        Entity carrier = event.getDismounted();
        if (!passenger.getPersistentDataContainer().has(Carrying.HELD)) return;
        boolean stays = event.isCancellable()
                && Carrying.holds(carrier, passenger)
                && carrier.isValid()
                && !carrier.isDead()
                && !passenger.isDead()
                && (!(passenger instanceof Player player) || player.isOnline());
        if (stays) {
            event.setCancelled(true);
            return;
        }
        Carrying.unmark(passenger);
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        boolean sneak = event.getInput().isSneak();
        boolean wasSneaking = Boolean.TRUE.equals(sneaking.put(player.getUniqueId(), sneak));
        boolean pressed = sneak && !wasSneaking;
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!pressed || !data.has(Carrying.HELD)) return;
        Integer before = data.get(Carrying.STRUGGLES, PersistentDataType.INTEGER);
        int struggles = (before == null ? 0 : before) + 1;
        if (struggles >= BREAK_FREE) {
            Carrying.release(player);
            return;
        }
        data.set(Carrying.STRUGGLES, PersistentDataType.INTEGER, struggles);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sneaking.remove(event.getPlayer().getUniqueId());
    }
}
