/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.editor.EditorChecks;
import de.eldoria.bloodnight.editor.EditorClient;
import de.eldoria.bloodnight.editor.EditorItems;
import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.editor.EditorResult;
import de.eldoria.bloodnight.editor.EditorSessionFile;
import de.eldoria.bloodnight.editor.SettingsPayload;
import de.eldoria.bloodnight.web.api.editor.SessionCreated;
import de.eldoria.bloodnight.web.api.editor.SessionView;
import de.eldoria.graph.payload.CatalogGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
 * {@code /bloodnight editor [new | apply [force] | cancel]}: opens a web editor session with the server's mobs,
 * macros, items and settings, applies the session once it is marked as done in the editor, or closes it.
 * <p>
 * A server keeps its session until it is applied or cancelled. Everyone who runs {@code /bloodnight editor} while it is
 * open joins it with a personal link under their name, so several players can edit at once, each holding the sections
 * they work on; they are told about files that changed on the server since it was opened. {@code new} replaces the
 * session with a fresh one.
 * <p>
 * Applying is the confirmation: nothing changes on the server before. The result is checked first (mob ids, items,
 * readable files, and that no file it replaces changed on disk since the session opened; {@code force} skips the
 * last check); a result that does not pass changes nothing and keeps the session open. Otherwise every file is
 * backed up, mobs, macros, items and settings are replaced, the configuration is reloaded and every mob recompiled.
 * Items registered in game keep everything but the name, lore and enchantments the editor sets.
 * <p>
 * Files and the session are handled on the global region, so a reload never races with itself.
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
        plugin.tasks().globalNow(() -> join(sender));
    }

    private void join(CommandSender sender) {
        Optional<SessionCreated> open = sessions().load();
        if (open.isEmpty()) {
            openNew(sender, Optional.empty());
            return;
        }
        client().addParticipant(open.get(), participant(sender))
                .whenComplete((joined, error) -> sync(() -> {
                    if (error != null) {
                        sender.sendMessage(Component.text(reason(error), NamedTextColor.RED));
                        return;
                    }
                    if (joined.isEmpty()) {
                        sender.sendMessage("The last editor session expired, opening a new one");
                        openNew(sender, Optional.empty());
                        return;
                    }
                    sender.sendMessage(
                            link("Continue editing here: ", joined.get().editorUrl()));
                    client().view(open.get())
                            .thenAccept(view -> view.ifPresent(found -> sync(() -> warnChanged(sender, found))));
                }));
    }

    @Executes("new")
    void openFresh(CommandSender sender) {
        plugin.tasks().globalNow(() -> openNew(sender, sessions().load()));
    }

    private void openNew(CommandSender sender, Optional<SessionCreated> previous) {
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
        client().open(plugin.catalog().version(), catalog, payload, participant(sender))
                .whenComplete((session, error) -> sync(() -> {
                    if (error != null) {
                        sender.sendMessage(Component.text(reason(error), NamedTextColor.RED));
                        return;
                    }
                    previous.ifPresent(old -> client().close(old));
                    sessions().save(session);
                    sender.sendMessage(link("Edit here: ", session.editorUrl()));
                }));
    }

    private static String participant(CommandSender sender) {
        return sender instanceof Player player ? player.getName() : "Console";
    }

    private static Component link(String prefix, String editorUrl) {
        return Component.text(prefix)
                .append(Component.text(editorUrl, NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl(editorUrl)))
                .append(Component.text(
                        ". When you are done, mark the session as done and run /bloodnight editor apply."));
    }

    private void warnChanged(CommandSender sender, SessionView view) {
        List<String> changed;
        try {
            changed = EditorChecks.changedFiles(
                    EditorChecks.hashes(view.payload()),
                    plugin.configuration().fileHashes(plugin.getServer().getWorlds()));
        } catch (IOException e) {
            return;
        }
        if (changed.isEmpty()) return;
        sender.sendMessage(Component.text(
                "These files changed on the server since the session was opened:", NamedTextColor.YELLOW));
        changed.forEach(file -> sender.sendMessage(" - " + file));
        sender.sendMessage("Applying the session would overwrite them; /bloodnight editor new starts over from the"
                + " server's files.");
    }

    @Executes("apply")
    void apply(CommandSender sender) {
        plugin.tasks().globalNow(() -> apply(sender, false));
    }

    @Executes("apply force")
    void applyForce(CommandSender sender) {
        plugin.tasks().globalNow(() -> apply(sender, true));
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
        plugin.tasks().globalNow(() -> close(sender));
    }

    private void close(CommandSender sender) {
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
        List<String> problems =
                EditorChecks.problems(result, plugin.configuration().items()::knows);
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
        plugin.configuration()
                .replaceItems(EditorItems.apply(plugin.configuration().items(), result.items()));
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
        plugin.tasks().global(task);
    }

    private static String reason(Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
