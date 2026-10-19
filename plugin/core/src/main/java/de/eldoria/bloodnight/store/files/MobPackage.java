/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store.files;

import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * One mob as a store ships it: {@code <id>.json} next to the store's {@code index.json}. The file describes the mob in
 * every language it has and carries its whole definition, the loot tables it assigns and the particle effects of its
 * own it plays, so it installs without anything else.
 *
 * @param format       the package format, {@value StoreFiles#FORMAT} for this one
 * @param id           the mob's id, the same as the definition's
 * @param pack         the id of the pack it belongs to
 * @param names        the mob's name by language, e.g. {@code en}
 * @param descriptions what the mob does, by language
 * @param requires     plugins it needs, e.g. {@code MythicMobs}; listed, not enforced
 * @param mob          the mob's definition, as in {@code mobs.yml}
 * @param lootTables   the loot tables the mob assigns, by id; added on install where the server has none of that id
 * @param particleEffects the particle effects of its own the mob plays, by id; built-in ones are not carried. Added
 *                     on install where the server has none of that id
 */
public record MobPackage(
        int format,
        String id,
        String pack,
        Map<String, String> names,
        Map<String, String> descriptions,
        List<String> requires,
        CustomMob mob,
        Map<String, LootTable> lootTables,
        Map<String, ParticleEffect> particleEffects) {
    public MobPackage {
        lootTables = lootTables == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(lootTables));
        particleEffects =
                particleEffects == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(particleEffects));
    }

    /**
     * A package without particle effects of its own.
     */
    public MobPackage(
            int format,
            String id,
            String pack,
            Map<String, String> names,
            Map<String, String> descriptions,
            List<String> requires,
            CustomMob mob,
            Map<String, LootTable> lootTables) {
        this(format, id, pack, names, descriptions, requires, mob, lootTables, Map.of());
    }
}
