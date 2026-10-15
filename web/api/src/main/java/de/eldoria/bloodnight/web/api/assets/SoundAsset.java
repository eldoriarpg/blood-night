/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.assets;

import java.util.Map;

/**
 * One sound of Minecraft's.
 *
 * @param group     what kind of sound it is, the first part of its key, e.g. {@code entity}, {@code ambient} or
 *                  {@code block}
 * @param subtitles what the game's subtitles say when it plays, by language, e.g. {@code en_us: Zombie groans}; empty
 *                  for the sounds without a subtitle, such as music
 * @param playable  whether {@link AssetRoutes#SOUND} can play it
 */
public record SoundAsset(String group, Map<String, String> subtitles, boolean playable) {
    public SoundAsset {
        subtitles = Map.copyOf(subtitles);
    }
}
