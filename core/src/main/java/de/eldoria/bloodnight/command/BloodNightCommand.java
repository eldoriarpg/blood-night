/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import net.strokkur.commands.Aliases;
import net.strokkur.commands.Command;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.paper.Description;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

/**
 * {@code /bloodnight}: administration of the plugin, one class per subcommand. Each subcommand has its own
 * permission, {@code bloodnight.command.<subcommand>}; {@code bloodnight.admin} grants all of them.
 */
@Command("bloodnight")
@Aliases("bn")
@Description("Blood night administration")
final class     BloodNightCommand {
    private static final String USAGE =
            "/bloodnight <reload | spawn <mob> | problems [mob] | source <mob> | debug [mob] | export>";

    @Subcommand("reload")
    @Permission("bloodnight.command.reload")
    final ReloadCommand reload;

    @Subcommand("spawn")
    @Permission("bloodnight.command.spawn")
    final SpawnCommand spawn;

    @Subcommand("problems")
    @Permission("bloodnight.command.problems")
    final ProblemsCommand problems;

    @Subcommand("source")
    @Permission("bloodnight.command.source")
    final SourceCommand source;

    @Subcommand("debug")
    @Permission("bloodnight.command.debug")
    final DebugCommand debug;

    @Subcommand("export")
    @Permission("bloodnight.command.export")
    final ExportCommand export;

    BloodNightCommand(BloodNight plugin) {
        reload = new ReloadCommand(plugin);
        spawn = new SpawnCommand(plugin);
        problems = new ProblemsCommand(plugin);
        source = new SourceCommand(plugin);
        debug = new DebugCommand(plugin);
        export = new ExportCommand(plugin);
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        sender.sendMessage("Usage: " + USAGE);
    }
}
