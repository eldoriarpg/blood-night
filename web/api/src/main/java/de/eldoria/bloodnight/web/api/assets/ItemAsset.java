/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.assets;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * One item of a Minecraft version, as the editor's pickers show it.
 *
 * @param texture its icon, e.g. {@code item/diamond_sword} under {@link AssetRoutes#TEXTURE} with {@code .png}
 *                appended; absent when none could be found
 * @param names   its name by language, e.g. {@code en_us} and {@code de_de}
 */
public record ItemAsset(@Nullable String texture, Map<String, String> names) {
    public ItemAsset {
        names = Map.copyOf(names);
    }
}
