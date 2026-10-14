/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight source <mob>}: writes the generated Java source of a mob to {@code debug/<mob>.java}.
 */
@Subcommand("source")
@Permission("bloodnight.command.source")
final class SourceCommand {
    private final BloodNight plugin;

    SourceCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        Replies.send(sender, "usage", arg("usage", "/bloodnight source <mob>"));
    }

    @Executes
    void source(CommandSender sender, @MobSuggestions String mob) {
        plugin.behaviours()
                .of(mob)
                .ifPresentOrElse(
                        compiled -> {
                            try {
                                Path file = DebugFolder.of(plugin).resolve(mob + ".java");
                                Files.writeString(file, compiled.source());
                                Replies.send(sender, "source.written", arg("mob", mob), arg("file", file));
                            } catch (IOException e) {
                                Replies.send(sender, "source.failed", arg("error", String.valueOf(e.getMessage())));
                            }
                        },
                        () -> Replies.send(sender, "source.missing", arg("mob", mob)));
    }
}
