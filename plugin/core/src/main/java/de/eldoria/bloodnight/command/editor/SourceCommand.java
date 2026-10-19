/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.editor;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.command.setup.DebugFolder;
import de.eldoria.bloodnight.command.suggest.MobSuggestions;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@code /bloodnight source <mob>}: writes the generated Java source of a mob to {@code debug/<mob>.java}.
 */
@Subcommand("source")
@Permission("bloodnight.command.source")
public final class SourceCommand {
    private final BloodNight plugin;

    public SourceCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    public void usage(CommandSender sender) {
        Replies.send(sender, "usage", Replies.arg("usage", "/bloodnight source <mob>"));
    }

    @Executes
    public void source(CommandSender sender, @MobSuggestions String mob) {
        plugin.behaviours()
                .of(mob)
                .ifPresentOrElse(
                        compiled -> {
                            try {
                                Path file = DebugFolder.of(plugin).resolve(mob + ".java");
                                Files.writeString(file, compiled.source());
                                Replies.send(
                                        sender, "source.written", Replies.arg("mob", mob), Replies.arg("file", file));
                            } catch (IOException e) {
                                Replies.send(
                                        sender, "source.failed", Replies.arg("reason", String.valueOf(e.getMessage())));
                            }
                        },
                        () -> Replies.send(sender, "source.missing", Replies.arg("mob", mob)));
    }
}
