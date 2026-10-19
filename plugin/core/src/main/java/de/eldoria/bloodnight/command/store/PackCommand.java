/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.store;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.announce.Texts;
import de.eldoria.bloodnight.atmosphere.pack.PackMerge;
import de.eldoria.bloodnight.command.Replies;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * {@code /bloodnight pack export}: writes the resource pack as a zip and a folder into {@code plugins/BloodNight/pack},
 * to merge it into another pack by hand, and puts it into the installed pack plugins again.
 */
@Subcommand("pack")
@Permission("bloodnight.command.pack")
public final class PackCommand {
    private final BloodNight plugin;

    public PackCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    public void usage(CommandSender sender) {
        Replies.send(sender, "usage", Replies.arg("usage", "/bloodnight pack export"));
    }

    @Executes("export")
    public void export(CommandSender sender) {
        Path directory = plugin.getDataFolder().toPath().resolve("pack");
        try {
            PackMerge pack = PackMerge.bundled().withMobNames(plugin.mobNames());
            pack.export(directory);
            Replies.send(sender, "pack.exported", Replies.arg("directory", directory));
            Path plugins = plugin.getDataFolder().toPath().toAbsolutePath().getParent();
            for (PackMerge.Target target : plugin.packTargets()) {
                pack.install(plugins, target);
                Replies.send(
                        sender,
                        "pack.merged",
                        Replies.arg("plugin", target.plugin()),
                        Replies.arg("reload", Texts.command(target.reload())));
            }
        } catch (IOException | UncheckedIOException e) {
            Replies.send(sender, "pack.failed", Replies.arg("reason", String.valueOf(e.getMessage())));
        }
    }
}
