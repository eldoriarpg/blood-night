/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.mob.CustomMob;

import java.util.List;
import java.util.Map;

/**
 * One mob as a store ships it: {@code <id>.json} next to the store's {@code index.json}. The file describes the mob in
 * every language it has and carries its whole definition, so it installs without anything else.
 *
 * @param format       the package format, {@value StoreFiles#FORMAT} for this one
 * @param id           the mob's id, the same as the definition's
 * @param pack         the id of the pack it belongs to
 * @param names        the mob's name by language, e.g. {@code en}
 * @param descriptions what the mob does, by language
 * @param requires     plugins it needs, e.g. {@code MythicMobs}; listed, not enforced
 * @param mob          the mob's definition, as in {@code mobs.yml}
 */
public record MobPackage(
        int format,
        String id,
        String pack,
        Map<String, String> names,
        Map<String, String> descriptions,
        List<String> requires,
        CustomMob mob) {}
