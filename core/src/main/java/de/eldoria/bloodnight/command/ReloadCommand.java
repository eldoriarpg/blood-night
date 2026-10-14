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
 * {@code /bloodnight reload}: reads the configuration files again and recompiles every mob.
 */
@Subcommand("reload")
@Permission("bloodnight.command.reload")
final class ReloadCommand {
    private final BloodNight plugin;

    ReloadCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    void reload(CommandSender sender) {
        plugin.tasks().globalNow(() -> reload(plugin, sender));
    }

    /**
     * Reads the configuration again, reports files that could not be read and recompiles every mob.
     *
     * @param plugin the plugin
     * @param sender who hears about it
     */
    static void reload(BloodNight plugin, CommandSender sender) {
        plugin.configuration().reload();
        plugin.configuration().mobs();
        plugin.configuration().items();
        plugin.configuration().macros();
        plugin.messages().fallback(plugin.configuration().main().announcements().fallbackLocale());
        plugin.bloodPack().refresh();
        plugin.configuration()
                .problems()
                .keySet()
                .forEach(file -> Replies.send(sender, "reload.unreadable", arg("file", file)));
        Replies.send(sender, "reload.compiling");
        plugin.recompile()
                .thenAccept(result -> Replies.send(
                        sender,
                        "reload.compiled",
                        arg("compiled", result.compiled().size()),
                        arg("cached", result.fromCache()),
                        arg("problems", result.problems().size())));
    }
}
