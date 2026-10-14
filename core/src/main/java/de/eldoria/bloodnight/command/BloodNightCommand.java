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

import java.util.LinkedHashMap;
import java.util.Map;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight}: administration of the plugin, one class per subcommand, each declaring its own path and
 * permission. Each subcommand has its own
 * permission, {@code bloodnight.command.<subcommand>}; {@code bloodnight.admin} grants all of them.
 */
@Command("bloodnight")
@Aliases("bn")
@Description("Blood night administration")
final class BloodNightCommand {
    private static final Map<String, String> SUBCOMMANDS = subcommands();
    private final BloodNight plugin;

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

    @Subcommand
    final ItemCommand item;

    @Subcommand
    final SettingsCommand settings;

    @Subcommand
    final StoreCommand store;

    BloodNightCommand(BloodNight plugin) {
        this.plugin = plugin;
        reload = new ReloadCommand(plugin);
        spawn = new SpawnCommand(plugin);
        problems = new ProblemsCommand(plugin);
        source = new SourceCommand(plugin);
        debug = new DebugCommand(plugin);
        export = new ExportCommand(plugin);
        night = new NightCommand(plugin);
        editor = new EditorCommand(plugin);
        item = new ItemCommand(plugin);
        settings = new SettingsCommand(plugin);
        store = new StoreCommand(plugin);
    }

    /**
     * The version, every subcommand the sender may use with a short description, and where to read more.
     */
    @DefaultExecutes
    void help(CommandSender sender) {
        Replies.send(
                sender, "help.header", arg("version", plugin.getPluginMeta().getVersion()));
        SUBCOMMANDS.forEach((name, syntax) -> {
            if (!sender.hasPermission("bloodnight.command." + name)) return;
            Replies.send(
                    sender,
                    "help.line",
                    arg("syntax", "/bloodnight " + syntax),
                    arg("description", Replies.text("help." + name)));
        });
        Replies.send(sender, "help.docs");
    }

    private static Map<String, String> subcommands() {
        Map<String, String> subcommands = new LinkedHashMap<>();
        subcommands.put("night", "night [force | cancel] [world]");
        subcommands.put("editor", "editor [new | apply [force] | cancel]");
        subcommands.put("store", "store [list <pack> | install <pack | mob> | update [pack | mob]]");
        subcommands.put("spawn", "spawn <mob>");
        subcommands.put("item", "item <register | remove <id>>");
        subcommands.put("settings", "settings <server | world> <path> [value]");
        subcommands.put("reload", "reload");
        subcommands.put("problems", "problems [mob]");
        subcommands.put("source", "source <mob>");
        subcommands.put("debug", "debug [mob]");
        subcommands.put("export", "export");
        return subcommands;
    }
}
