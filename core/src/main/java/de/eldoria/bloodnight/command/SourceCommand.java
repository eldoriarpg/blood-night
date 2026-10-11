/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@code /bloodnight source <mob>}: writes the generated Java source of a mob to {@code debug/<mob>.java}.
 */
final class SourceCommand {
    private final BloodNight plugin;

    SourceCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        sender.sendMessage("Usage: /bloodnight source <mob>");
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
                                sender.sendMessage("Source of %s written to %s".formatted(mob, file));
                            } catch (IOException e) {
                                sender.sendMessage("Could not write source: " + e.getMessage());
                            }
                        },
                        () -> sender.sendMessage("Mob %s has no compiled behaviour".formatted(mob)));
    }
}
