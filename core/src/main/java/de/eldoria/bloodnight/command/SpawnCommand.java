/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.mob.CustomMob;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * {@code /bloodnight spawn <mob>}: spawns a mob where a player stands, or at the spawn of the first world for the
 * console.
 */
final class SpawnCommand {
    private final BloodNight plugin;

    SpawnCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        sender.sendMessage("Usage: /bloodnight spawn <mob>");
    }

    @Executes
    void spawn(CommandSender sender, @MobSuggestions String mob) {
        CustomMob definition = plugin.configuration().mobs().get(mob);
        if (definition == null) {
            sender.sendMessage("Unknown mob " + mob);
            return;
        }
        Location location = sender instanceof Player player
                ? player.getLocation()
                : plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        Entity entity = location.getWorld()
                .spawnEntity(location, definition.attributes().entityType());
        if (!(entity instanceof LivingEntity living)) {
            entity.remove();
            sender.sendMessage("Mob %s is not a living entity type".formatted(mob));
            return;
        }
        plugin.coordinator().register(living, definition);
        sender.sendMessage("Spawned %s at %d %d %d"
                .formatted(mob, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
