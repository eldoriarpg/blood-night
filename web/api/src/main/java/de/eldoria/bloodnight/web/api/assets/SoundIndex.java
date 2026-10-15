/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.assets;

import java.util.Map;

/**
 * Every sound of a Minecraft version, by what players hear.
 *
 * @param version the version, e.g. {@code 26.3}
 * @param sounds  the sounds by their event key without namespace, e.g. {@code entity.zombie.ambient}
 */
public record SoundIndex(String version, Map<String, SoundAsset> sounds) {
    public SoundIndex {
        sounds = Map.copyOf(sounds);
    }
}
