/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.editor;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.api.event.SettingsChangeCause;
import de.eldoria.bloodnight.command.ReloadCommand;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.command.setup.ConvertCommand;
import de.eldoria.bloodnight.editor.EditorChecks;
import de.eldoria.bloodnight.editor.EditorClient;
import de.eldoria.bloodnight.editor.EditorResult;
import de.eldoria.bloodnight.editor.EditorSessionFile;
import de.eldoria.bloodnight.editor.item.EditorItems;
import de.eldoria.bloodnight.editor.payload.EditorLegacy;
import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.bloodnight.editor.payload.SettingsPayload;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.loot.LootTables;
import de.eldoria.bloodnight.particles.ParticleEffects;
import de.eldoria.bloodnight.web.api.editor.EditorArea;
import de.eldoria.bloodnight.web.api.editor.SessionState;
import de.eldoria.bloodnight.web.api.editor.SessionView;
import de.eldoria.bloodnight.web.api.editor.session.SessionCreated;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * {@code /bloodnight editor [new | apply [force] | cancel]}: opens a web editor session with the server's mobs,
 * macros, items and settings, applies the session each time it is marked as done in the editor, or closes it.
 * <p>
 * A server keeps its session until it is cancelled, replaced or expires; applying keeps it open, so it can be edited
 * and applied again. After an apply the server tells the service the applied revision and its file hashes, which the
 * next apply's conflict check uses. Everyone who runs {@code /bloodnight editor} while it is
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
public final class EditorCommand {
    /**
     * The start of the permission for each editor area, e.g. {@code bloodnight.editor.mobs}.
     */
    static final String AREA_PERMISSION = "bloodnight.editor.";

    private final BloodNight plugin;
    private @Nullable EditorClient client;

    public EditorCommand(BloodNight plugin) {
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
    public void open(CommandSender sender) {
        plugin.tasks().globalNow(() -> join(sender));
    }

    private void join(CommandSender sender) {
        Optional<SessionCreated> open = sessions().load();
        if (open.isEmpty()) {
            openNew(sender, Optional.empty());
            return;
        }
        client().addParticipant(open.get(), participant(sender), areas(sender))
                .whenComplete((joined, error) -> sync(() -> {
                    if (error != null) {
                        Replies.send(sender, "editor.failed", Replies.arg("reason", reason(error)));
                        return;
                    }
                    if (joined.isEmpty()) {
                        Replies.send(sender, "editor.expired");
                        openNew(sender, Optional.empty());
                        return;
                    }
                    Replies.send(
                            sender,
                            "editor.continue",
                            Replies.arg("link", link(joined.get().editorUrl())));
                    client().view(open.get())
                            .thenAccept(view -> view.ifPresent(found -> sync(() -> warnChanged(sender, found))));
                }));
    }

    @Executes("new")
    public void openFresh(CommandSender sender) {
        plugin.tasks().globalNow(() -> openNew(sender, sessions().load()));
    }

    private void openNew(CommandSender sender, Optional<SessionCreated> previous) {
        open(sender, previous, false);
    }

    /**
     * Replaces any open session with one the editor starts with the setup wizard, the old BloodNight's files converted
     * when the server has them.
     *
     * @param sender who runs the setup
     */
    public void openSetup(CommandSender sender) {
        plugin.tasks().globalNow(() -> open(sender, sessions().load(), true));
    }

    private void open(CommandSender sender, Optional<SessionCreated> previous, boolean setup) {
        String payload;
        try {
            EditorPayload opened = EditorPayload.of(
                    plugin.configuration(),
                    plugin.behaviours(),
                    plugin.catalog().version(),
                    plugin.getServer().getWorlds(),
                    plugin.store(),
                    plugin.worldNames()::alias);
            payload = (setup ? opened.forSetup(legacy(sender)) : opened).asJson();
        } catch (IOException e) {
            Replies.send(sender, "editor.unreadable", Replies.arg("reason", String.valueOf(e.getMessage())));
            return;
        }
        String catalog = CatalogGenerator.write(plugin.catalog());
        Replies.send(sender, "editor.opening");
        client().open(plugin.catalog().version(), catalog, payload, participant(sender), areas(sender))
                .whenComplete((session, error) -> sync(() -> {
                    if (error != null) {
                        Replies.send(sender, "editor.failed", Replies.arg("reason", reason(error)));
                        return;
                    }
                    previous.ifPresent(old -> client().close(old));
                    sessions().save(session);
                    Replies.send(
                            sender,
                            setup ? "setup.opened" : "editor.opened",
                            Replies.arg("link", link(session.editorUrl())));
                }));
    }

    private @Nullable EditorLegacy legacy(CommandSender sender) {
        if (!Files.isDirectory(ConvertCommand.legacyFolder(plugin))) return null;
        try {
            return EditorLegacy.of(
                    ConvertCommand.convert(plugin), plugin.getServer().getWorlds());
        } catch (IOException | RuntimeException e) {
            Replies.send(sender, "convert.failed", Replies.arg("reason", String.valueOf(e.getMessage())));
            return null;
        }
    }

    /**
     * @return the areas the sender may change: those of {@code bloodnight.editor.<area>} they have, everything for the
     *     console
     */
    static Set<EditorArea> areas(CommandSender sender) {
        Set<EditorArea> areas = EnumSet.noneOf(EditorArea.class);
        for (EditorArea area : EditorArea.values()) {
            if (sender.hasPermission(AREA_PERMISSION + area.name().toLowerCase(Locale.ROOT))) areas.add(area);
        }
        return areas;
    }

    private static String participant(CommandSender sender) {
        return sender instanceof Player player ? player.getName() : "Console";
    }

    private static Component link(String editorUrl) {
        return Component.text(editorUrl, NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl(editorUrl));
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
        Replies.send(sender, "editor.changed");
        changed.forEach(file -> Replies.send(sender, "editor.item", Replies.arg("item", file)));
        Replies.send(sender, "editor.changed_hint");
    }

    @Executes("apply")
    public void apply(CommandSender sender) {
        plugin.tasks().globalNow(() -> apply(sender, false));
    }

    @Executes("apply force")
    public void applyForce(CommandSender sender) {
        plugin.tasks().globalNow(() -> apply(sender, true));
    }

    private void apply(CommandSender sender, boolean force) {
        Optional<SessionCreated> session = sessions().load();
        if (session.isEmpty()) {
            Replies.send(sender, "editor.none_open");
            return;
        }
        Replies.send(sender, "editor.pulling");
        client().view(session.get())
                .thenCompose(current -> current.filter(view -> view.state() == SessionState.APPLIED)
                                .isPresent()
                        ? CompletableFuture.<@Nullable SessionView>completedFuture(null)
                        : client().pull(session.get()))
                .whenComplete((view, error) -> sync(() -> {
                    if (error != null) {
                        Replies.send(sender, "editor.failed", Replies.arg("reason", reason(error)));
                        return;
                    }
                    if (view == null) {
                        Replies.send(sender, "editor.nothing_new");
                        return;
                    }
                    if (apply(sender, view, force)) applied(sender, session.get(), view.revision());
                }));
    }

    /**
     * Tells the service what this apply wrote, so the session stays open for the next one.
     */
    private void applied(CommandSender sender, SessionCreated session, int revision) {
        Map<String, String> hashes;
        try {
            hashes = plugin.configuration().fileHashes(plugin.getServer().getWorlds());
        } catch (IOException e) {
            Replies.send(sender, "editor.unreadable", Replies.arg("reason", String.valueOf(e.getMessage())));
            return;
        }
        client().applied(session, revision, hashes)
                .whenComplete((ignored, error) -> sync(() -> {
                    if (error != null) Replies.send(sender, "editor.failed", Replies.arg("reason", reason(error)));
                    else Replies.send(sender, "editor.stays_open");
                }));
    }

    @Executes("cancel")
    public void cancel(CommandSender sender) {
        plugin.tasks().globalNow(() -> close(sender));
    }

    private void close(CommandSender sender) {
        Optional<SessionCreated> session = sessions().load();
        if (session.isEmpty()) {
            Replies.send(sender, "editor.none");
            return;
        }
        sessions().clear();
        client().close(session.get());
        Replies.send(sender, "editor.closed");
    }

    private boolean apply(CommandSender sender, SessionView view, boolean force) {
        EditorResult result;
        try {
            result = EditorResult.of(view.payload());
        } catch (RuntimeException e) {
            Replies.send(sender, "editor.session_unreadable", Replies.arg("reason", String.valueOf(e.getMessage())));
            return false;
        }
        ItemRegistry items = itemsBefore(result, view.payload().path("setup").asBoolean(false));
        List<String> problems = EditorChecks.problems(result, items::knows);
        if (!problems.isEmpty()) {
            Replies.send(sender, "editor.not_applied");
            problems.forEach(problem -> Replies.send(sender, "editor.item", Replies.arg("item", problem)));
            return false;
        }
        if (!force && !unchanged(sender, result)) return false;
        Map<String, World> worlds = plugin.getServer().getWorlds().stream()
                .collect(Collectors.toMap(world -> world.getUID().toString(), world -> world));
        Path backup;
        try {
            backup = plugin.configuration().backup();
        } catch (IOException e) {
            Replies.send(sender, "editor.backup_failed", Replies.arg("reason", String.valueOf(e.getMessage())));
            return false;
        }
        plugin.configuration().replaceMobs(result.mobs().values());
        plugin.configuration().replaceMacros(result.macros());
        plugin.configuration().replaceItems(EditorItems.apply(items, result.items()));
        plugin.configuration().replaceLootTables(new LootTables(result.lootTables()));
        plugin.configuration().replaceParticleEffects(new ParticleEffects(result.particleEffects()));
        SettingsPayload settings = result.settings();
        if (settings != null) {
            plugin.configuration().replaceMain(settings.main());
            settings.worlds().forEach((uid, world) -> {
                World target = worlds.get(uid);
                if (target != null) plugin.configuration().replaceWorld(target, world.settings());
            });
            Set<String> known = worlds.keySet();
            EditorChecks.unknownWorlds(result, known)
                    .forEach(uid -> Replies.send(
                            sender,
                            "editor.unknown_world",
                            Replies.arg("world", settings.worlds().get(uid).name()),
                            Replies.arg("uid", uid)));
        }
        Replies.send(
                sender,
                "editor.applied",
                Replies.arg("mobs", result.mobs().size()),
                Replies.arg("macros", result.macros().size()),
                Replies.arg("backup", backup));
        ReloadCommand.reload(plugin, sender, SettingsChangeCause.EDITOR);
        return true;
    }

    /**
     * The items the result is applied onto: the server's, and for a setup session the registered items of the old
     * BloodNight the result kept, which the editor cannot carry.
     */
    private ItemRegistry itemsBefore(EditorResult result, boolean setup) {
        ItemRegistry current = plugin.configuration().items();
        if (!setup || !Files.isDirectory(ConvertCommand.legacyFolder(plugin))) return current;
        try {
            return EditorItems.adopt(
                    current,
                    ConvertCommand.convert(plugin).items(),
                    result.items().keySet());
        } catch (IOException | RuntimeException e) {
            return current;
        }
    }

    private boolean unchanged(CommandSender sender, EditorResult result) {
        List<String> changed;
        try {
            changed = EditorChecks.changedFiles(
                    result, plugin.configuration().fileHashes(plugin.getServer().getWorlds()));
        } catch (IOException e) {
            Replies.send(sender, "editor.unreadable", Replies.arg("reason", String.valueOf(e.getMessage())));
            return false;
        }
        if (changed.isEmpty()) return true;
        Replies.send(sender, "editor.conflict");
        changed.forEach(file -> Replies.send(sender, "editor.item", Replies.arg("item", file)));
        Replies.send(sender, "editor.conflict_hint");
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
