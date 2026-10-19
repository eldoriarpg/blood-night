/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.editor;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.command.setup.DebugFolder;
import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.graph.payload.CatalogGenerator;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@code /bloodnight export}: writes the editor payload and catalog to {@code debug/}.
 */
@Subcommand("export")
@Permission("bloodnight.command.export")
public final class ExportCommand {
    private final BloodNight plugin;

    public ExportCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    public void export(CommandSender sender) {
        try {
            Path directory = DebugFolder.of(plugin);
            Files.writeString(
                    directory.resolve("payload.json"),
                    EditorPayload.of(
                                    plugin.configuration(),
                                    plugin.behaviours(),
                                    plugin.catalog().version(),
                                    plugin.getServer().getWorlds(),
                                    plugin.store(),
                                    plugin.worldNames()::alias)
                            .asJson());
            Files.writeString(directory.resolve("catalog.json"), CatalogGenerator.write(plugin.catalog()));
            Replies.send(sender, "export.written", Replies.arg("directory", directory));
        } catch (IOException e) {
            Replies.send(sender, "export.failed", Replies.arg("reason", String.valueOf(e.getMessage())));
        }
    }
}
