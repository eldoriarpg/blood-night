/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.legacy;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The old plugin's settings as plain maps, as its YAML holds them.
 *
 * @param general its {@code generalSettings}
 * @param worlds  each world's settings, by world name
 */
public record Legacy(Map<String, Object> general, Map<String, Map<String, Object>> worlds) {
    /**
     * @return the value as a map, empty when it is none
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> map(@Nullable Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    /**
     * @return the value as a list, empty when it is none
     */
    static List<?> list(@Nullable Object value) {
        return value instanceof List<?> list ? list : List.of();
    }
}
