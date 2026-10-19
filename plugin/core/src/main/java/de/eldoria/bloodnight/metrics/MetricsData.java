/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.metrics;

import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.mob.CustomMob;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * What BloodNight tells bStats, worked out from the server's settings: no names, worlds or players, only counts and
 * choices. Read from bStats' own thread, so it only reads settings.
 */
public final class MetricsData {
    /**
     * Plugins BloodNight works with, counted when installed.
     */
    static final List<String> INTEGRATIONS = List.of(
            "PlaceholderAPI",
            "MiniPlaceholders",
            "WorldGuard",
            "Vault",
            "MythicMobs",
            "Multiverse-Core",
            "Hyperverse",
            "Nexo",
            "Oraxen",
            "ItemsAdder");

    private MetricsData() {}

    /**
     * @return how many worlds with blood nights pick them which way, by selection type
     */
    static Map<String, Integer> nightSelections(Collection<WorldSettings> worlds) {
        Map<String, Integer> types = new TreeMap<>();
        for (WorldSettings world : worlds) {
            if (world.enabled()) types.merge(world.nightSelection().type().name(), 1, Integer::sum);
        }
        return types;
    }

    /**
     * @return how many mobs come from the store and how many are the server's own
     */
    static Map<String, Integer> mobOrigins(Collection<CustomMob> mobs) {
        Map<String, Integer> origins = new TreeMap<>();
        for (CustomMob mob : mobs) origins.merge(mob.origin() == null ? "own" : "store", 1, Integer::sum);
        return origins;
    }

    /**
     * @param installed whether a plugin, by name, is installed
     * @return each installed plugin BloodNight works with, counted once
     */
    static Map<String, Integer> integrations(Predicate<String> installed) {
        Map<String, Integer> found = new TreeMap<>();
        for (String plugin : INTEGRATIONS) {
            if (installed.test(plugin)) found.put(plugin, 1);
        }
        return found;
    }

    /**
     * @param url the configured editor service
     * @return whether the server uses the hosted editor or its own
     */
    static String editorService(String url, String hosted) {
        return url.equals(hosted) ? "hosted" : "own";
    }
}
