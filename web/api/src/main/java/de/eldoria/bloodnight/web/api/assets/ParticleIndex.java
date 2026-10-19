/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.assets;

import java.util.List;
import java.util.Map;

/**
 * The sprites of each particle of a Minecraft version, for the editor's particle previews.
 *
 * @param version   the version, e.g. {@code 26.3}
 * @param particles the sprites of each particle by its key, e.g. {@code minecraft:flame} to {@code particle/flame}, in
 *                  the order the game animates them; served at {@link AssetRoutes#TEXTURE}. Particles drawn from a
 *                  block or an item, such as {@code minecraft:block}, have none.
 */
public record ParticleIndex(String version, Map<String, List<String>> particles) {
    public ParticleIndex {
        particles = Map.copyOf(particles);
    }
}
