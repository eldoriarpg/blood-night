/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.announce.Texts;
import de.eldoria.bloodnight.atmosphere.PackMerge;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight pack export}: writes the resource pack as a zip and a folder into {@code plugins/BloodNight/pack},
 * to merge it into another pack by hand, and puts it into the installed pack plugins again.
 */
@Subcommand("pack")
@Permission("bloodnight.command.pack")
final class PackCommand {
    private final BloodNight plugin;

    PackCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        Replies.send(sender, "usage", arg("usage", "/bloodnight pack export"));
    }

    @Executes("export")
    void export(CommandSender sender) {
        Path directory = plugin.getDataFolder().toPath().resolve("pack");
        try {
            PackMerge pack = PackMerge.bundled().withMobNames(plugin.mobNames());
            pack.export(directory);
            Replies.send(sender, "pack.exported", arg("directory", directory));
            Path plugins = plugin.getDataFolder().toPath().toAbsolutePath().getParent();
            for (PackMerge.Target target : plugin.packTargets()) {
                pack.install(plugins, target);
                Replies.send(
                        sender,
                        "pack.merged",
                        arg("plugin", target.plugin()),
                        arg("reload", Texts.command(target.reload())));
            }
        } catch (IOException | UncheckedIOException e) {
            Replies.send(sender, "pack.failed", arg("reason", String.valueOf(e.getMessage())));
        }
    }
}
