/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bloodnight particles [on|off]}: switches the particle effects of mobs off or on for the player who runs it,
 * or tells whether they see them. Every player may use it.
 */
@Subcommand("particles")
@Permission("bloodnight.command.particles")
public final class ParticlesCommand {
    private final BloodNight plugin;

    public ParticlesCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    public void show(CommandSender sender) {
        if (sender instanceof Player player) tell(player, plugin.viewers().visible(player));
        else Replies.send(sender, "particles.players");
    }

    @Executes("on")
    public void on(CommandSender sender) {
        set(sender, true);
    }

    @Executes("off")
    public void off(CommandSender sender) {
        set(sender, false);
    }

    private void set(CommandSender sender, boolean visible) {
        if (!(sender instanceof Player player)) {
            Replies.send(sender, "particles.players");
            return;
        }
        plugin.tasks().entityNow(player, () -> tell(player, plugin.viewers().set(player, visible)));
    }

    private static void tell(Player player, boolean visible) {
        Replies.send(player, visible ? "particles.shown" : "particles.hidden");
    }
}
