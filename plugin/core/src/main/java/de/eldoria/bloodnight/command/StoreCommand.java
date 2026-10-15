/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.MobOrigin;
import de.eldoria.bloodnight.store.MobStore;
import de.eldoria.bloodnight.store.StoreEntry;
import de.eldoria.bloodnight.store.StorePack;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight store}: lists the store's packs and mobs, installs them and takes their updates. Installed and
 * updated mobs are written to {@code mobs.yml} and built at once.
 */
@Subcommand("store")
@Permission("bloodnight.command.store")
final class StoreCommand {
    private final BloodNight plugin;

    StoreCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void packs(CommandSender sender) {
        for (StorePack pack : store().store().packs()) {
            long size = store().store().entries().stream()
                    .filter(entry -> entry.pack().equals(pack.id()))
                    .count();
            Replies.send(
                    sender,
                    "store.pack",
                    arg("id", pack.id()),
                    arg("name", text(sender, pack.names(), pack.id())),
                    arg("count", size),
                    arg("description", text(sender, pack.descriptions(), "")));
        }
        Replies.send(sender, "store.hint");
    }

    @Executes("list")
    void list(CommandSender sender, @StoreSuggestions String pack) {
        plugin.tasks().globalNow(() -> {
            Map<String, CustomMob> installed = installed();
            List<StoreEntry> entries = store().store().resolve(pack);
            if (entries.isEmpty()) {
                Replies.send(sender, "store.unknown", arg("id", pack));
                return;
            }
            for (StoreEntry entry : entries) {
                CustomMob mob = installed.get(entry.id());
                Replies.send(
                        sender,
                        "store.entry",
                        arg("id", entry.id()),
                        arg("name", text(sender, entry.mobPackage().names(), entry.id())),
                        arg("state", Replies.text(sender, "store.state." + state(mob))),
                        arg("description", text(sender, entry.mobPackage().descriptions(), "")));
            }
        });
    }

    @Executes("install")
    void install(CommandSender sender, @StoreSuggestions String packOrEntry) {
        plugin.tasks().globalNow(() -> {
            if (store().store().resolve(packOrEntry).isEmpty()) {
                Replies.send(sender, "store.unknown", arg("id", packOrEntry));
                return;
            }
            MobStore.Change change =
                    store().install(plugin.configuration().mobs().all(), packOrEntry);
            change.skipped().forEach(id -> Replies.send(sender, "store.taken", arg("id", id)));
            apply(sender, change, "store.installed");
        });
    }

    @Executes("update")
    void updateAll(CommandSender sender) {
        plugin.tasks()
                .globalNow(() -> apply(
                        sender, store().update(plugin.configuration().mobs().all(), mob -> true), "store.updated"));
    }

    @Executes("update")
    void update(CommandSender sender, @StoreSuggestions String packOrEntry) {
        plugin.tasks().globalNow(() -> {
            List<String> ids = store().store().resolve(packOrEntry).stream()
                    .map(StoreEntry::id)
                    .toList();
            apply(
                    sender,
                    store().update(plugin.configuration().mobs().all(), mob -> fromAny(mob, ids)),
                    "store.updated");
        });
    }

    private void apply(CommandSender sender, MobStore.Change change, String done) {
        if (change.done().isEmpty()) {
            Replies.send(sender, "store.nothing");
            return;
        }
        plugin.configuration().replaceMobs(change.mobs());
        Replies.send(sender, done, arg("mobs", String.join(", ", change.done())));
        ReloadCommand.reload(plugin, sender);
    }

    private String state(CustomMob mob) {
        return mob == null ? "available" : store().state(mob).name().toLowerCase(Locale.ROOT);
    }

    private Map<String, CustomMob> installed() {
        Map<String, CustomMob> installed = new HashMap<>();
        for (CustomMob mob : plugin.configuration().mobs().all()) {
            MobOrigin origin = mob.origin();
            if (origin != null) installed.putIfAbsent(origin.entry(), mob);
        }
        return installed;
    }

    private static boolean fromAny(CustomMob mob, List<String> entries) {
        MobOrigin origin = mob.origin();
        return origin != null && entries.contains(origin.entry());
    }

    private MobStore store() {
        return plugin.store();
    }

    private static String text(CommandSender sender, Map<String, String> byLanguage, String fallback) {
        String language = sender instanceof Player player ? player.locale().getLanguage() : "en";
        return byLanguage.getOrDefault(language, byLanguage.getOrDefault("en", fallback));
    }
}
