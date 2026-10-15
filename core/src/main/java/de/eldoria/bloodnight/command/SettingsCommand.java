/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.SettingsPaths;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.arguments.StringArg;
import net.strokkur.commands.arguments.StringArgType;
import net.strokkur.commands.permission.Permission;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight settings <target> <path> [value]}: shows or changes one setting of {@code config.yml} (target
 * {@code server}) or of a world, without the web editor. A change is checked like a change from the editor, written
 * to the file and used at once. Mobs, items and macros stay with the editor.
 */
@Subcommand("settings")
@Permission("bloodnight.command.settings")
final class SettingsCommand {
    private static final String USAGE = "/bloodnight settings <server | world> <path> [value]";
    private final BloodNight plugin;

    SettingsCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        Replies.send(sender, "usage", arg("usage", USAGE));
    }

    @Executes
    void show(CommandSender sender, @SettingsTargetSuggestions String target, @SettingsPathSuggestions String path) {
        plugin.tasks().globalNow(() -> {
            if (SettingsTargets.SERVER.equals(target)) {
                show(sender, path, plugin.configuration().main(), new ConfigFile());
                return;
            }
            world(sender, target)
                    .ifPresent(world ->
                            show(sender, path, plugin.configuration().worldConfig(world), new WorldSettings()));
        });
    }

    @Executes
    void set(
            CommandSender sender,
            @SettingsTargetSuggestions String target,
            @SettingsPathSuggestions String path,
            @StringArg(StringArgType.GREEDY) String value) {
        plugin.tasks().globalNow(() -> {
            try {
                if (SettingsTargets.SERVER.equals(target)) {
                    ConfigFile changed =
                            SettingsPaths.write(plugin.configuration().main(), ConfigFile.class, path, value);
                    plugin.configuration().replaceMain(changed);
                    plugin.applyTextSettings();
                    plugin.bloodPack().refresh();
                    show(sender, "settings.set", path, changed);
                    return;
                }
                Optional<World> world = world(sender, target);
                if (world.isEmpty()) return;
                WorldSettings changed = SettingsPaths.write(
                        plugin.configuration().worldConfig(world.get()), WorldSettings.class, path, value);
                plugin.configuration().replaceWorld(world.get(), changed);
                show(sender, "settings.set", path, changed);
            } catch (IllegalArgumentException e) {
                Replies.send(
                        sender, "settings.invalid", arg("path", path), arg("reason", String.valueOf(e.getMessage())));
            }
        });
    }

    private void show(CommandSender sender, String path, Object settings, Object defaults) {
        Optional<JsonNode> value = SettingsPaths.read(settings, path);
        if (value.isEmpty()) {
            Replies.send(sender, "settings.unknown", arg("path", path));
            return;
        }
        Replies.send(
                sender,
                "settings.value",
                arg("path", path),
                arg("value", text(value.get())),
                arg(
                        "default",
                        SettingsPaths.read(defaults, path)
                                .map(SettingsCommand::text)
                                .orElse("-")));
    }

    private static void show(CommandSender sender, String key, String path, Object settings) {
        Replies.send(
                sender,
                key,
                arg("path", path),
                arg(
                        "value",
                        SettingsPaths.read(settings, path)
                                .map(SettingsCommand::text)
                                .orElse("-")));
    }

    private static String text(JsonNode value) {
        return value.isString() ? value.asString() : value.toString();
    }

    private Optional<World> world(CommandSender sender, String name) {
        World world = plugin.getServer().getWorld(name);
        if (world == null) Replies.send(sender, "night.unknown_world", arg("world", name));
        return Optional.ofNullable(world);
    }
}
