/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.setup;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.debug.DebugReport;
import de.eldoria.eldoutilities.debug.DebugUtil;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

/**
 * {@code /bloodnight debug}: uploads a debug report to Lyna and answers with its links, as the other Eldoria plugins
 * do. The first time on a server it explains what is sent and asks to run the command again (see {@link DebugUtil}).
 */
@Subcommand("debug")
@Permission("bloodnight.command.debug")
public final class DebugCommand {
    private final BloodNight plugin;

    public DebugCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    public void report(CommandSender sender) {
        DebugUtil.dispatchDebug(sender, plugin, DebugReport.settings());
    }
}
