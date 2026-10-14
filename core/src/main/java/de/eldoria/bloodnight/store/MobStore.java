/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.MobOrigin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Installs and updates mobs from a store. Every method takes the server's mobs and returns the new set, so the
 * caller decides when to write them.
 * <p>
 * An installed mob records its entry and the hash of the logic it brought ({@link MobOrigin}). An update replaces only
 * the logic ({@link MobLogic}) and keeps the server's tuning.
 */
public final class MobStore {
    private final Store store;

    /**
     * @param store the store mobs are installed from
     */
    public MobStore(Store store) {
        this.store = store;
    }

    /**
     * @return the store
     */
    public Store store() {
        return store;
    }

    /**
     * @return how the mob stands to the store
     */
    public StoreState state(CustomMob mob) {
        MobOrigin origin = mob.origin();
        if (origin == null) return StoreState.OWN;
        Optional<StoreEntry> entry = entry(origin);
        if (entry.isEmpty()) return StoreState.ORPHANED;
        boolean changed = !MobLogic.hash(mob).equals(origin.hash());
        boolean outdated = !entry.get().hash().equals(origin.hash());
        if (changed) return outdated ? StoreState.CHANGED_OUTDATED : StoreState.CHANGED;
        return outdated ? StoreState.OUTDATED : StoreState.CURRENT;
    }

    /**
     * Installs a pack or one entry. Entries whose id the server already uses are skipped.
     *
     * @return the new mobs and what happened
     */
    public Change install(Collection<CustomMob> mobs, String packOrEntry) {
        Map<String, CustomMob> result = byId(mobs);
        List<String> done = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (StoreEntry entry : store.resolve(packOrEntry)) {
            if (result.containsKey(entry.id())) {
                skipped.add(entry.id());
                continue;
            }
            result.put(entry.id(), entry.mob().withOrigin(origin(entry)));
            done.add(entry.id());
        }
        return new Change(List.copyOf(result.values()), done, skipped);
    }

    /**
     * Updates the logic of the mobs that match and have a newer version in the store.
     *
     * @param which the mobs to update, e.g. by state
     * @return the new mobs and what happened
     */
    public Change update(Collection<CustomMob> mobs, Predicate<CustomMob> which) {
        Map<String, CustomMob> result = byId(mobs);
        List<String> done = new ArrayList<>();
        for (CustomMob mob : mobs) {
            StoreState state = state(mob);
            if (state != StoreState.OUTDATED && state != StoreState.CHANGED_OUTDATED) continue;
            MobOrigin origin = mob.origin();
            if (origin == null || !which.test(mob)) continue;
            StoreEntry entry = entry(origin).orElseThrow();
            result.put(
                    mob.id(),
                    mob.withLogic(entry.mob().behaviour(), entry.mob().extension())
                            .withOrigin(origin(entry)));
            done.add(mob.id());
        }
        return new Change(List.copyOf(result.values()), done, List.of());
    }

    /**
     * Updates every mob nobody changed since it was installed.
     */
    public Change autoUpdate(Collection<CustomMob> mobs) {
        return update(mobs, mob -> state(mob) == StoreState.OUTDATED);
    }

    private Optional<StoreEntry> entry(MobOrigin origin) {
        return origin.store().equals(store.id()) ? store.entry(origin.entry()) : Optional.empty();
    }

    private MobOrigin origin(StoreEntry entry) {
        return new MobOrigin(store.id(), entry.id(), entry.hash());
    }

    private static Map<String, CustomMob> byId(Collection<CustomMob> mobs) {
        Map<String, CustomMob> byId = new LinkedHashMap<>();
        mobs.forEach(mob -> byId.put(mob.id(), mob));
        return byId;
    }

    /**
     * The mobs after an install or update.
     *
     * @param mobs    every mob of the server afterwards
     * @param done    the ids installed or updated
     * @param skipped the ids skipped because the server already has a mob of that id
     */
    public record Change(List<CustomMob> mobs, List<String> done, List<String> skipped) {}
}
