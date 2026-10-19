/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.night;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.command.suggest.MobSuggestions;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

/**
 * {@code /bloodnight trigger [mob]}: fires the debug entry on every mob, or on the mobs of one id.
 */
@Subcommand("trigger")
@Permission("bloodnight.command.trigger")
public final class TriggerCommand {
    private final BloodNight plugin;

    public TriggerCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    public void all(CommandSender sender) {
        Replies.send(
                sender,
                "trigger.fired",
                Replies.arg("count", plugin.coordinator().debug(null)));
    }

    @Executes
    public void mob(CommandSender sender, @MobSuggestions String mob) {
        Replies.send(
                sender,
                "trigger.fired",
                Replies.arg("count", plugin.coordinator().debug(mob)));
    }
}
