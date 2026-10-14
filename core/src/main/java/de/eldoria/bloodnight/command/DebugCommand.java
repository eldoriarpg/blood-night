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

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight debug [mob]}: fires the debug entry on every mob, or on the mobs of one id.
 */
@Subcommand("debug")
@Permission("bloodnight.command.debug")
final class DebugCommand {
    private final BloodNight plugin;

    DebugCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    void all(CommandSender sender) {
        Replies.send(sender, "debug.fired", arg("count", plugin.coordinator().debug(null)));
    }

    @Executes
    void mob(CommandSender sender, @MobSuggestions String mob) {
        Replies.send(sender, "debug.fired", arg("count", plugin.coordinator().debug(mob)));
    }
}
