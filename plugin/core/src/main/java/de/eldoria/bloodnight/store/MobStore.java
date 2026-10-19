/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobOrigin;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import de.eldoria.bloodnight.store.build.MobLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Installs and updates mobs from a store. Every method takes the server's mobs and returns the new set, so the
 * caller decides when to write them.
 * <p>
 * An installed mob records its entry and the hash of the logic it brought ({@link MobOrigin}). An update replaces only
 * the logic ({@link MobLogic}) and keeps the server's tuning. Installing adds the loot tables and particle effects a
 * mob brings where the server has none of that id; neither an install nor an update replaces a table the server has.
 * <p>
 * The particle effects a mob wears are tuning too. An update asked to take the store's looks replaces them with the
 * store's, also for a mob whose logic is current, and replaces the server's effects of the ids the package brings.
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
     * @return whether the effects the mob wears differ from those of its store entry, which an update can take
     */
    public boolean looksDiffer(CustomMob mob) {
        MobOrigin origin = mob.origin();
        if (origin == null) return false;
        return entry(origin)
                .map(entry -> !Objects.equals(mob.particles(), entry.mob().particles()))
                .orElse(false);
    }

    /**
     * Installs a pack or one entry. Entries whose id the server already uses are skipped.
     *
     * @param tables the ids of the server's loot tables
     * @return the new mobs, the loot tables they bring that the server lacks, and what happened
     */
    public Change install(Collection<CustomMob> mobs, Set<String> tables, String packOrEntry) {
        return install(mobs, tables, Set.of(), packOrEntry);
    }

    /**
     * Installs a pack or one entry. Entries whose id the server already uses are skipped.
     *
     * @param tables  the ids of the server's loot tables
     * @param effects the ids of the server's own particle effects
     * @return the new mobs, the loot tables and particle effects they bring that the server lacks, and what happened
     */
    public Change install(Collection<CustomMob> mobs, Set<String> tables, Set<String> effects, String packOrEntry) {
        Map<String, CustomMob> result = byId(mobs);
        Map<String, LootTable> added = new TreeMap<>();
        Map<String, ParticleEffect> addedEffects = new TreeMap<>();
        List<String> done = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (StoreEntry entry : store.resolve(packOrEntry)) {
            if (result.containsKey(entry.id())) {
                skipped.add(entry.id());
                continue;
            }
            result.put(entry.id(), entry.mob().withOrigin(origin(entry)));
            entry.mobPackage().lootTables().forEach((id, table) -> {
                if (!tables.contains(id)) added.putIfAbsent(id, table);
            });
            entry.mobPackage().particleEffects().forEach((id, effect) -> {
                if (!effects.contains(id)) addedEffects.putIfAbsent(id, effect);
            });
            done.add(entry.id());
        }
        return new Change(List.copyOf(result.values()), added, addedEffects, done, skipped);
    }

    /**
     * Updates the logic of the mobs that match and have a newer version in the store; their looks stay.
     *
     * @param which the mobs to update, e.g. by state
     * @return the new mobs and what happened
     */
    public Change update(Collection<CustomMob> mobs, Predicate<CustomMob> which) {
        return update(mobs, which, false);
    }

    /**
     * Updates the logic of the mobs that match and have a newer version in the store.
     *
     * @param which     the mobs to update, e.g. by state
     * @param particles whether they also take the particle effects their store entry wears and brings, even when
     *                  their logic is current
     * @return the new mobs, the particle effects to write, and what happened
     */
    public Change update(Collection<CustomMob> mobs, Predicate<CustomMob> which, boolean particles) {
        Map<String, CustomMob> result = byId(mobs);
        Map<String, ParticleEffect> effects = new TreeMap<>();
        List<String> done = new ArrayList<>();
        for (CustomMob mob : mobs) {
            MobOrigin origin = mob.origin();
            if (origin == null || !which.test(mob)) continue;
            Optional<StoreEntry> found = entry(origin);
            if (found.isEmpty()) continue;
            StoreEntry entry = found.get();
            StoreState state = state(mob);
            boolean logic = state == StoreState.OUTDATED || state == StoreState.CHANGED_OUTDATED;
            boolean looks = particles && looksDiffer(mob);
            if (!logic && !looks) continue;
            CustomMob updated = logic
                    ? mob.withLogic(
                                    entry.mob().behaviour(),
                                    entry.mob().extension(),
                                    entry.mob().movement())
                            .withOrigin(origin(entry))
                    : mob;
            if (particles) {
                updated = updated.withParticles(entry.mob().particles());
                effects.putAll(entry.mobPackage().particleEffects());
            }
            result.put(mob.id(), updated);
            done.add(mob.id());
        }
        return new Change(List.copyOf(result.values()), Map.of(), effects, done, List.of());
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
     * @param tables  the loot tables to add, which the installed mobs assign and the server lacks
     * @param effects the particle effects to write, replacing those of the same id
     * @param done    the ids installed or updated
     * @param skipped the ids skipped because the server already has a mob of that id
     */
    public record Change(
            List<CustomMob> mobs,
            Map<String, LootTable> tables,
            Map<String, ParticleEffect> effects,
            List<String> done,
            List<String> skipped) {}
}
