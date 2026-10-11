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
import org.bukkit.command.CommandSender;

/**
 * {@code /bloodnight}: administration of the plugin, one class per subcommand, each declaring its own path and
 * permission. Each subcommand has its own
 * permission, {@code bloodnight.command.<subcommand>}; {@code bloodnight.admin} grants all of them.
 */
@Command("bloodnight")
@Aliases("bn")
@Description("Blood night administration")
final class BloodNightCommand {
    private static final String USAGE =
            "/bloodnight <reload | spawn <mob> | problems [mob] | source <mob> | debug [mob] | export"
                    + " | night [force | cancel] [world] | editor [new | apply [force] | cancel]>";

    @Subcommand
    final ReloadCommand reload;

    @Subcommand
    final SpawnCommand spawn;

    @Subcommand
    final ProblemsCommand problems;

    @Subcommand
    final SourceCommand source;

    @Subcommand
    final DebugCommand debug;

    @Subcommand
    final ExportCommand export;

    @Subcommand
    final NightCommand night;

    @Subcommand
    final EditorCommand editor;

    BloodNightCommand(BloodNight plugin) {
        reload = new ReloadCommand(plugin);
        spawn = new SpawnCommand(plugin);
        problems = new ProblemsCommand(plugin);
        source = new SourceCommand(plugin);
        debug = new DebugCommand(plugin);
        export = new ExportCommand(plugin);
        night = new NightCommand(plugin);
        editor = new EditorCommand(plugin);
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        sender.sendMessage("Usage: " + USAGE);
    }
}
