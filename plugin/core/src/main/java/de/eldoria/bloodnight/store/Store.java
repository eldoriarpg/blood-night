/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.store.files.StorePack;

import java.util.List;
import java.util.Optional;

/**
 * Mobs to install, grouped in packs.
 *
 * @param id      the store's id, e.g. {@code bloodnight}
 * @param packs   its packs
 * @param entries its mobs
 */
public record Store(String id, List<StorePack> packs, List<StoreEntry> entries) {
    /**
     * @return the entry of an id, if the store has it
     */
    public Optional<StoreEntry> entry(String id) {
        return entries.stream().filter(entry -> entry.id().equals(id)).findFirst();
    }

    /**
     * @return the entries of a pack, or the one entry of that id; empty when neither exists
     */
    public List<StoreEntry> resolve(String packOrEntry) {
        List<StoreEntry> pack = entries.stream()
                .filter(entry -> entry.pack().equals(packOrEntry))
                .toList();
        return pack.isEmpty() ? entry(packOrEntry).stream().toList() : pack;
    }
}
