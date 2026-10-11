/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import de.eldoria.bloodnight.night.BloodNights;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Makes phantoms come for every player in a world with a running blood night by marking them as not having slept for
 * days. The player's own time since rest is kept in their data container and given back when the blood night ends or
 * they leave the world; a player who was still marked when the server stopped gets it back on their next join.
 */
public final class PhantomForce implements Listener {
    /**
     * Time since rest the phantoms are sure to come at: ten days.
     */
    static final int RESTLESS = 720_000;

    static final NamespacedKey RESTED = new NamespacedKey("bloodnight", "time_since_rest");
    private final BloodNights nights;
    private final Predicate<World> force;

    /**
     * @param nights the running blood nights
     * @param force  whether a world forces phantoms
     */
    public PhantomForce(BloodNights nights, Predicate<World> force) {
        this.nights = nights;
        this.force = force;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        if (!force.test(event.getWorld())) return;
        event.getWorld().getPlayers().forEach(PhantomForce::mark);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        event.getWorld().getPlayers().forEach(PhantomForce::restore);
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        restore(event.getPlayer());
        entered(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        restore(event.getPlayer());
        entered(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restore(event.getPlayer());
    }

    /**
     * Gives every marked player their own time since rest back, e.g. when the plugin disables.
     */
    public void restoreAll(Collection<? extends Player> players) {
        players.forEach(PhantomForce::restore);
    }

    private void entered(Player player) {
        World world = player.getWorld();
        if (nights.running(world).isPresent() && force.test(world)) mark(player);
    }

    static void mark(Player player) {
        var data = player.getPersistentDataContainer();
        if (!data.has(RESTED, PersistentDataType.INTEGER)) {
            data.set(RESTED, PersistentDataType.INTEGER, player.getStatistic(Statistic.TIME_SINCE_REST));
        }
        player.setStatistic(Statistic.TIME_SINCE_REST, RESTLESS);
    }

    static void restore(Player player) {
        var data = player.getPersistentDataContainer();
        Integer rested = data.get(RESTED, PersistentDataType.INTEGER);
        if (rested == null) return;
        player.setStatistic(Statistic.TIME_SINCE_REST, rested);
        data.remove(RESTED);
    }
}
