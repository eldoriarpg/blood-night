/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.editor.EditorChecks;
import de.eldoria.bloodnight.editor.EditorClient;
import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.editor.EditorResult;
import de.eldoria.bloodnight.editor.EditorSessionFile;
import de.eldoria.bloodnight.editor.SettingsPayload;
import de.eldoria.bloodnight.editor.api.SessionCreated;
import de.eldoria.bloodnight.editor.api.SessionView;
import de.eldoria.graph.payload.CatalogGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * {@code /bloodnight editor [apply [force] | cancel]}: opens a web editor session with the server's mobs, macros,
 * items and settings, applies the session once it is marked as done in the editor, or closes it.
 * <p>
 * Applying is the confirmation: nothing changes on the server before. The result is checked first (mob ids, items,
 * readable files, and that no file it replaces changed on disk since the session opened; {@code force} skips the
 * last check); a result that does not pass changes nothing and keeps the session open. Otherwise every file is
 * backed up, mobs, macros and settings are replaced, the configuration is reloaded and every mob recompiled. Items
 * are never changed by the editor.
 */
@Subcommand("editor")
@Permission("bloodnight.command.editor")
final class EditorCommand {
    private final BloodNight plugin;
    private @Nullable EditorClient client;

    EditorCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    private EditorClient client() {
        if (client == null) {
            client = new EditorClient(
                    HttpClient.newHttpClient(),
                    () -> plugin.configuration().main().editor());
        }
        return client;
    }

    private EditorSessionFile sessions() {
        return new EditorSessionFile(plugin.getDataFolder().toPath().resolve("editor-session.json"));
    }

    @Executes
    void open(CommandSender sender) {
        String payload;
        try {
            payload = EditorPayload.of(
                            plugin.configuration(),
                            plugin.behaviours(),
                            plugin.catalog().version(),
                            plugin.getServer().getWorlds())
                    .asJson();
        } catch (IOException e) {
            sender.sendMessage(Component.text("The files cannot be read: " + e.getMessage(), NamedTextColor.RED));
            return;
        }
        String catalog = CatalogGenerator.write(plugin.catalog());
        sender.sendMessage("Opening an editor session...");
        Optional<SessionCreated> previous = sessions().load();
        client().open(plugin.catalog().version(), catalog, payload)
                .whenComplete((session, error) -> sync(() -> {
                    if (error != null) {
                        sender.sendMessage(Component.text(reason(error), NamedTextColor.RED));
                        return;
                    }
                    previous.ifPresent(old -> client().close(old));
                    sessions().save(session);
                    sender.sendMessage(
                            Component.text("Edit here: ")
                                    .append(Component.text(session.editorUrl(), NamedTextColor.AQUA)
                                            .clickEvent(ClickEvent.openUrl(session.editorUrl())))
                                    .append(
                                            Component.text(
                                                    ". When you are done, mark the session as done and run /bloodnight editor apply.")));
                }));
    }

    @Executes("apply")
    void apply(CommandSender sender) {
        apply(sender, false);
    }

    @Executes("apply force")
    void applyForce(CommandSender sender) {
        apply(sender, true);
    }

    private void apply(CommandSender sender, boolean force) {
        Optional<SessionCreated> session = sessions().load();
        if (session.isEmpty()) {
            sender.sendMessage("There is no open editor session, open one with /bloodnight editor");
            return;
        }
        sender.sendMessage("Pulling the editor session...");
        client().pull(session.get())
                .whenComplete((view, error) -> sync(() -> {
                    if (error != null) {
                        sender.sendMessage(Component.text(reason(error), NamedTextColor.RED));
                        return;
                    }
                    if (apply(sender, view, force)) {
                        client().close(session.get());
                        sessions().clear();
                    }
                }));
    }

    @Executes("cancel")
    void cancel(CommandSender sender) {
        Optional<SessionCreated> session = sessions().load();
        if (session.isEmpty()) {
            sender.sendMessage("There is no open editor session");
            return;
        }
        sessions().clear();
        client().close(session.get());
        sender.sendMessage("The editor session was closed");
    }

    private boolean apply(CommandSender sender, SessionView view, boolean force) {
        EditorResult result;
        try {
            result = EditorResult.of(view.payload());
        } catch (RuntimeException e) {
            sender.sendMessage(Component.text("The session cannot be read: " + e.getMessage(), NamedTextColor.RED));
            return false;
        }
        List<String> problems = EditorChecks.problems(
                result, plugin.configuration().items().registrations().keySet());
        if (!problems.isEmpty()) {
            sender.sendMessage(Component.text("The session was not applied:", NamedTextColor.RED));
            problems.forEach(problem -> sender.sendMessage(" - " + problem));
            return false;
        }
        if (!force && !unchanged(sender, result)) return false;
        Map<String, World> worlds = plugin.getServer().getWorlds().stream()
                .collect(Collectors.toMap(world -> world.getUID().toString(), world -> world));
        Path backup;
        try {
            backup = plugin.configuration().backup();
        } catch (IOException e) {
            sender.sendMessage(Component.text(
                    "Could not back up the files, nothing was changed: " + e.getMessage(), NamedTextColor.RED));
            return false;
        }
        plugin.configuration().replaceMobs(result.mobs().values());
        plugin.configuration().replaceMacros(result.macros());
        SettingsPayload settings = result.settings();
        if (settings != null) {
            plugin.configuration().replaceMain(settings.main());
            plugin.configuration().replaceGlobalDrops(settings.globalDrops());
            settings.worlds().forEach((uid, world) -> {
                World target = worlds.get(uid);
                if (target != null) plugin.configuration().replaceWorld(target, world.settings());
            });
            Set<String> known = worlds.keySet();
            EditorChecks.unknownWorlds(result, known)
                    .forEach(uid -> sender.sendMessage("Skipped the settings of unknown world %s (%s)"
                            .formatted(settings.worlds().get(uid).name(), uid)));
        }
        sender.sendMessage("Applied %d mobs and %d macros, previous files are in %s"
                .formatted(result.mobs().size(), result.macros().size(), backup));
        ReloadCommand.reload(plugin, sender);
        return true;
    }

    private boolean unchanged(CommandSender sender, EditorResult result) {
        List<String> changed;
        try {
            changed = EditorChecks.changedFiles(
                    result, plugin.configuration().fileHashes(plugin.getServer().getWorlds()));
        } catch (IOException e) {
            sender.sendMessage(Component.text("The files cannot be read: " + e.getMessage(), NamedTextColor.RED));
            return false;
        }
        if (changed.isEmpty()) return true;
        sender.sendMessage(Component.text(
                "The session was not applied, these files changed since it was opened:", NamedTextColor.RED));
        changed.forEach(file -> sender.sendMessage(" - " + file));
        sender.sendMessage("Run /bloodnight editor apply force to overwrite them anyway; they are backed up first.");
        return false;
    }

    private void sync(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    private static String reason(Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
