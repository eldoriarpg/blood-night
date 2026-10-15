/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.api.event.SettingsChangeCause;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.legacy.LegacyConverter;
import de.eldoria.bloodnight.legacy.LegacyFolder;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight convert}: takes over the old BloodNight's settings from {@code legacy/}. Without {@code apply} it
 * only shows what would change and what cannot be taken over; {@code apply} backs up the current files first.
 */
@Subcommand("convert")
@Permission("bloodnight.command.convert")
final class ConvertCommand {
    private final BloodNight plugin;

    ConvertCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void dryRun(CommandSender sender) {
        plugin.tasks().globalNow(() -> convert(sender, false));
    }

    @Executes("apply")
    void apply(CommandSender sender) {
        plugin.tasks().globalNow(() -> convert(sender, true));
    }

    private void convert(CommandSender sender, boolean apply) {
        Path folder = legacyFolder(plugin);
        if (!Files.isDirectory(folder)) {
            Replies.send(sender, "convert.missing", arg("folder", folder));
            return;
        }
        LegacyConverter.Result result;
        try {
            result = convert(plugin);
        } catch (IOException | RuntimeException e) {
            Replies.send(sender, "convert.failed", arg("reason", String.valueOf(e.getMessage())));
            return;
        }
        Replies.send(
                sender,
                apply ? "convert.applying" : "convert.preview",
                arg("worlds", String.join(", ", result.worlds().keySet())),
                arg("mobs", result.mobs().size()),
                arg("items", result.items().registrations().size()));
        result.notes().forEach(note -> Replies.send(sender, "convert.note", arg("note", note)));
        if (!apply) {
            Replies.send(sender, "convert.hint");
            return;
        }
        try {
            Path backup = plugin.configuration().backup();
            plugin.configuration().replaceMain(result.main());
            result.worlds().forEach((name, settings) -> {
                World world = plugin.getServer().getWorld(name);
                if (world != null) plugin.configuration().replaceWorld(world, settings);
            });
            plugin.configuration().replaceMobs(result.mobs());
            plugin.configuration().replaceItems(result.items());
            Replies.send(sender, "convert.done", arg("backup", backup));
            ReloadCommand.reload(plugin, sender, SettingsChangeCause.CONVERT);
        } catch (IOException e) {
            Replies.send(sender, "convert.failed", arg("reason", String.valueOf(e.getMessage())));
        }
    }

    /**
     * @param plugin the plugin
     * @return where the old BloodNight's files are expected
     */
    static Path legacyFolder(BloodNight plugin) {
        return plugin.getDataFolder().toPath().resolve(LegacyFolder.FOLDER);
    }

    /**
     * @param plugin the plugin
     * @return the old BloodNight's files converted onto the server's current state
     * @throws IOException when the old files cannot be read
     */
    static LegacyConverter.Result convert(BloodNight plugin) throws IOException {
        return LegacyConverter.onServer(LegacyFolder.read(legacyFolder(plugin)), plugin.store())
                .convert(
                        plugin.configuration().main(),
                        loadedWorlds(plugin),
                        plugin.configuration().mobs().all(),
                        plugin.configuration().items());
    }

    private static Map<String, WorldSettings> loadedWorlds(BloodNight plugin) {
        Map<String, WorldSettings> worlds = new LinkedHashMap<>();
        for (World world : plugin.getServer().getWorlds()) {
            worlds.put(world.getName(), plugin.configuration().worldConfig(world));
        }
        return worlds;
    }
}
