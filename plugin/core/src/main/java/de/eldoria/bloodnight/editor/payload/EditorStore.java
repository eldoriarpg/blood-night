/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.payload;

import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobOrigin;
import de.eldoria.bloodnight.store.MobStore;
import de.eldoria.bloodnight.store.StoreEntry;
import de.eldoria.bloodnight.store.StoreState;
import de.eldoria.bloodnight.store.files.StorePack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The store as the editor shows it: what can be installed, and how the installed store mobs stood to it when the
 * session opened. Read only in the session; installing adds the offered mob, updating takes its logic.
 *
 * @param packs  the packs
 * @param offers the mobs, ready to install
 * @param states the state of each store mob of the server by mob id, as it was when the session opened
 */
public record EditorStore(List<StorePack> packs, List<Offer> offers, Map<String, StoreState> states) {
    /**
     * @return the store and the server's mobs as the editor sees them
     */
    public static EditorStore of(MobStore store, Collection<CustomMob> mobs) {
        List<Offer> offers = store.store().entries().stream()
                .map(entry -> Offer.of(entry, store.store().id()))
                .toList();
        Map<String, StoreState> states = new TreeMap<>();
        for (CustomMob mob : mobs) {
            if (mob.origin() != null) states.put(mob.id(), store.state(mob));
        }
        return new EditorStore(store.store().packs(), offers, states);
    }

    /**
     * @return a store with nothing in it
     */
    public static EditorStore empty() {
        return new EditorStore(List.of(), List.of(), Map.of());
    }

    /**
     * A mob the store offers.
     *
     * @param id           the id it installs as
     * @param pack         the pack it belongs to
     * @param names        its name by language
     * @param descriptions what it does, by language
     * @param requires     plugins it needs
     * @param hash         the hash of its logic; an installed mob with another origin hash is outdated
     * @param mob          the mob as it installs, with its origin
     * @param lootTables   the loot tables the mob assigns, by id; installing adds those the session lacks
     */
    public record Offer(
            String id,
            String pack,
            Map<String, String> names,
            Map<String, String> descriptions,
            List<String> requires,
            String hash,
            CustomMob mob,
            Map<String, LootTable> lootTables) {
        static Offer of(StoreEntry entry, String store) {
            var mobPackage = entry.mobPackage();
            return new Offer(
                    entry.id(),
                    entry.pack(),
                    mobPackage.names(),
                    mobPackage.descriptions(),
                    mobPackage.requires(),
                    entry.hash(),
                    entry.mob().withOrigin(new MobOrigin(store, entry.id(), entry.hash())),
                    mobPackage.lootTables());
        }
    }
}
