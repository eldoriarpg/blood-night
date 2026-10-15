/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A special mob as the server has it configured.
 *
 * @param id         the mob's id, e.g. {@code armored_zombie}
 * @param style      MiniMessage opening tags put before its name in every language, e.g. {@code <dark_red>}; may be
 *                   empty
 * @param names      its name as plain text by language code, e.g. {@code en} or {@code pt_br}; empty when it shows no
 *                   name
 * @param entityType the kind of entity it takes over
 * @param store      the store it was installed from, empty for a mob the server made itself
 * @param weight     how often it is picked compared to the others, before any world's own weight
 */
public record MobInfo(
        String id, String style, Map<String, String> names, EntityType entityType, Optional<String> store, int weight) {
    public MobInfo {
        names = Map.copyOf(names);
    }

    /**
     * @param language a language code, e.g. {@code de}
     * @return the name in that language, else in English, else the first by language code; empty without any
     */
    public String name(String language) {
        String text = names.get(language);
        if (text != null) return text;
        String english = names.get("en");
        if (english != null) return english;
        return new TreeMap<>(names).values().stream().findFirst().orElse("");
    }
}
