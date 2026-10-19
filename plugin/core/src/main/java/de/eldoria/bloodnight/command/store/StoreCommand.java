/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.store;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.api.event.SettingsChangeCause;
import de.eldoria.bloodnight.command.ReloadCommand;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.loot.LootTables;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobOrigin;
import de.eldoria.bloodnight.particles.ParticleEffects;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import de.eldoria.bloodnight.store.MobStore;
import de.eldoria.bloodnight.store.StoreEntry;
import de.eldoria.bloodnight.store.files.StorePack;
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

/**
 * {@code /bloodnight store}: lists the store's packs and mobs, installs them and takes their updates. Installed and
 * updated mobs are written to {@code mobs.yml} and built at once. An update keeps the particle effects a mob wears;
 * {@code update --particles} takes the store's as well.
 */
@Subcommand("store")
@Permission("bloodnight.command.store")
public final class StoreCommand {
    private final BloodNight plugin;

    public StoreCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    public void packs(CommandSender sender) {
        for (StorePack pack : store().store().packs()) {
            long size = store().store().entries().stream()
                    .filter(entry -> entry.pack().equals(pack.id()))
                    .count();
            Replies.send(
                    sender,
                    "store.pack",
                    Replies.arg("id", pack.id()),
                    Replies.arg("name", text(sender, pack.names(), pack.id())),
                    Replies.arg("count", size),
                    Replies.arg("description", text(sender, pack.descriptions(), "")));
        }
        Replies.send(sender, "store.hint");
    }

    @Executes("list")
    public void list(CommandSender sender, @StoreSuggestions String pack) {
        plugin.tasks().globalNow(() -> {
            Map<String, CustomMob> installed = installed();
            List<StoreEntry> entries = store().store().resolve(pack);
            if (entries.isEmpty()) {
                Replies.send(sender, "store.unknown", Replies.arg("id", pack));
                return;
            }
            for (StoreEntry entry : entries) {
                CustomMob mob = installed.get(entry.id());
                Replies.send(
                        sender,
                        "store.entry",
                        Replies.arg("id", entry.id()),
                        Replies.arg("name", text(sender, entry.mobPackage().names(), entry.id())),
                        Replies.arg("state", Replies.text(sender, "store.state." + state(mob))),
                        Replies.arg(
                                "description", text(sender, entry.mobPackage().descriptions(), "")));
            }
        });
    }

    @Executes("install")
    public void install(CommandSender sender, @StoreSuggestions String packOrEntry) {
        plugin.tasks().globalNow(() -> {
            if (store().store().resolve(packOrEntry).isEmpty()) {
                Replies.send(sender, "store.unknown", Replies.arg("id", packOrEntry));
                return;
            }
            MobStore.Change change = store().install(
                            plugin.configuration().mobs().all(),
                            plugin.configuration().lootTables().tables().keySet(),
                            plugin.configuration().particleEffects().effects().keySet(),
                            packOrEntry);
            change.skipped().forEach(id -> Replies.send(sender, "store.taken", Replies.arg("id", id)));
            apply(sender, change, "store.installed");
        });
    }

    @Executes("update")
    public void updateAll(CommandSender sender) {
        updateAll(sender, false);
    }

    @Executes("update")
    public void update(CommandSender sender, @StoreSuggestions String packOrEntry) {
        update(sender, packOrEntry, false);
    }

    /**
     * Updates every store mob and takes the particle effects its store entry wears, too.
     */
    @Executes("update --particles")
    public void updateAllWithParticles(CommandSender sender) {
        updateAll(sender, true);
    }

    /**
     * Updates the mobs of a pack or one mob and takes the particle effects their store entries wear, too.
     */
    @Executes("update --particles")
    public void updateWithParticles(CommandSender sender, @StoreSuggestions String packOrEntry) {
        update(sender, packOrEntry, true);
    }

    private void updateAll(CommandSender sender, boolean particles) {
        plugin.tasks()
                .globalNow(() -> apply(
                        sender,
                        store().update(plugin.configuration().mobs().all(), mob -> true, particles),
                        "store.updated"));
    }

    private void update(CommandSender sender, String packOrEntry, boolean particles) {
        plugin.tasks().globalNow(() -> {
            List<String> ids = store().store().resolve(packOrEntry).stream()
                    .map(StoreEntry::id)
                    .toList();
            apply(
                    sender,
                    store().update(plugin.configuration().mobs().all(), mob -> fromAny(mob, ids), particles),
                    "store.updated");
        });
    }

    private void apply(CommandSender sender, MobStore.Change change, String done) {
        if (change.done().isEmpty()) {
            Replies.send(sender, "store.nothing");
            return;
        }
        plugin.configuration().replaceMobs(change.mobs());
        if (!change.tables().isEmpty()) {
            Map<String, LootTable> tables =
                    new HashMap<>(plugin.configuration().lootTables().tables());
            tables.putAll(change.tables());
            plugin.configuration().replaceLootTables(new LootTables(tables));
        }
        if (!change.effects().isEmpty()) {
            Map<String, ParticleEffect> effects =
                    new HashMap<>(plugin.configuration().particleEffects().effects());
            effects.putAll(change.effects());
            plugin.configuration().replaceParticleEffects(new ParticleEffects(effects));
        }
        Replies.send(sender, done, Replies.arg("mobs", String.join(", ", change.done())));
        ReloadCommand.reload(plugin, sender, SettingsChangeCause.STORE);
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
