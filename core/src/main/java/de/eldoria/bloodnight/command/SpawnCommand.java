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
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight spawn <mob>}: spawns a mob on the block a player looks at, within {@value #REACH} blocks, or where
 * they stand when they look at none; for the console at the spawn of the first world. The mob is spawned on the thread
 * owning that place.
 */
@Subcommand("spawn")
@Permission("bloodnight.command.spawn")
final class SpawnCommand {
    static final int REACH = 64;
    private final BloodNight plugin;

    SpawnCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        Replies.send(sender, "usage", arg("usage", "/bloodnight spawn <mob>"));
    }

    @Executes
    void spawn(CommandSender sender, @MobSuggestions String mob) {
        CustomMob definition = plugin.configuration().mobs().get(mob);
        if (definition == null) {
            Replies.send(sender, "spawn.unknown", arg("mob", mob));
            return;
        }
        Location location = sender instanceof Player player
                ? lookedAt(player)
                : plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        plugin.tasks().at(location, () -> spawn(sender, mob, location));
    }

    /**
     * @return the middle of the face of the block the player looks at, so the mob stands on it or beside it; where the
     *     player stands when no block is within reach
     */
    static Location lookedAt(Player player) {
        RayTraceResult hit = player.rayTraceBlocks(REACH, FluidCollisionMode.NEVER);
        Block block = hit == null ? null : hit.getHitBlock();
        BlockFace face = hit == null ? null : hit.getHitBlockFace();
        if (block == null || face == null) return player.getLocation();
        return besides(block, face);
    }

    /**
     * @return the middle of the block next to the given one on that face, at its bottom
     */
    static Location besides(Block block, BlockFace face) {
        return block.getRelative(face).getLocation().add(0.5, 0, 0.5);
    }

    private void spawn(CommandSender sender, String mob, Location location) {
        if (plugin.service().spawn(mob, location).isEmpty()) {
            Replies.send(sender, "spawn.not_living", arg("mob", mob));
            return;
        }
        Replies.send(
                sender,
                "spawn.spawned",
                arg("mob", mob),
                arg("x", location.getBlockX()),
                arg("y", location.getBlockY()),
                arg("z", location.getBlockZ()));
    }
}
