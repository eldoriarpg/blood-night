/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who sees particle effects: players near enough who did not switch them off for themselves. The switch is kept on the
 * player ({@value #KEY}) and mirrored in memory while they are online, so effects never read a player's data from
 * another thread.
 * <p>
 * Players are found by walking the world's player list and comparing distances, the way vanilla picks who gets a
 * particle; Folia allows that from any region's thread.
 */
public final class Viewers implements Listener {
    /** The key of the switch on the player. */
    public static final String KEY = "bloodnight:particles_off";

    private static final NamespacedKey OFF = new NamespacedKey("bloodnight", "particles_off");
    private final Set<UUID> off = ConcurrentHashMap.newKeySet();

    /**
     * @param world    where the effect is
     * @param near     a point of the effect
     * @param distance how far away players still see it
     * @param only     the only player who may see it, or {@code null} for everyone
     * @param skip     players who do not see it, e.g. the one wearing wings
     * @return the players who see it
     */
    public List<Player> near(World world, Vec3 near, double distance, @Nullable Player only, Collection<Player> skip) {
        double squared = distance * distance;
        List<Player> seeing = new ArrayList<>();
        for (Player player : only == null ? world.getPlayers() : List.of(only)) {
            if (skip.contains(player) || off.contains(player.getUniqueId()) || player.getWorld() != world) continue;
            var location = player.getLocation();
            double x = location.getX() - near.x();
            double y = location.getY() - near.y();
            double z = location.getZ() - near.z();
            if (x * x + y * y + z * z <= squared) seeing.add(player);
        }
        return seeing;
    }

    /**
     * Switches effects off or on for a player. Runs on the player's thread.
     *
     * @return whether they see effects now
     */
    public boolean set(Player player, boolean visible) {
        if (visible) {
            off.remove(player.getUniqueId());
            player.getPersistentDataContainer().remove(OFF);
        } else {
            off.add(player.getUniqueId());
            player.getPersistentDataContainer().set(OFF, PersistentDataType.BOOLEAN, true);
        }
        return visible;
    }

    /**
     * @return the players online who switched effects off
     */
    public Set<UUID> off() {
        return Set.copyOf(off);
    }

    /**
     * @return whether the player sees effects
     */
    public boolean visible(Player player) {
        return !off.contains(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(OFF)) off.add(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        off.remove(event.getPlayer().getUniqueId());
    }
}
