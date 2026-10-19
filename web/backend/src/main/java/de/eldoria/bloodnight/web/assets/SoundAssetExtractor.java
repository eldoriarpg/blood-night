/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.assets;

import de.eldoria.bloodnight.web.api.assets.SoundAsset;
import de.eldoria.bloodnight.web.api.assets.SoundIndex;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Reads the sounds of a Minecraft version out of its {@code sounds.json} and asset index.
 * <p>
 * A sound event names its subtitle and the files it plays from; the first file is the one the editor plays, looked up
 * by its name in the asset index, whose hash is where Mojang serves it. An event that only refers to other events, or
 * whose file the index lacks, is listed but not playable.
 */
final class SoundAssetExtractor {
    private SoundAssetExtractor() {}

    /**
     * @param index the sounds
     * @param files the asset hash of each playable sound's file, by its key
     */
    record Extraction(SoundIndex index, Map<String, String> files) {}

    /**
     * @param version   the Minecraft version
     * @param sounds    its {@code sounds.json}
     * @param objects   the {@code objects} of its asset index
     * @param languages translations by language code, e.g. {@code en_us}
     */
    static Extraction extract(
            String version, JsonNode sounds, JsonNode objects, Map<String, Map<String, String>> languages) {
        Map<String, SoundAsset> assets = new TreeMap<>();
        Map<String, String> files = new TreeMap<>();
        sounds.properties().forEach(entry -> {
            String key = entry.getKey();
            JsonNode event = entry.getValue();
            Optional<String> hash = firstFile(event)
                    .map(name ->
                            objects.path("minecraft/sounds/" + name + ".ogg").path("hash"))
                    .filter(JsonNode::isString)
                    .map(JsonNode::asString);
            hash.ifPresent(found -> files.put(key, found));
            assets.put(
                    key,
                    new SoundAsset(
                            group(key), subtitles(event.path("subtitle").asString(""), languages), hash.isPresent()));
        });
        return new Extraction(new SoundIndex(version, assets), files);
    }

    private static Optional<String> firstFile(JsonNode event) {
        for (JsonNode sound : event.path("sounds")) {
            if (sound.isString()) return Optional.of(sound.asString());
            if (!sound.path("type").asString("file").equals("event")
                    && sound.path("name").isString()) {
                return Optional.of(sound.path("name").asString());
            }
        }
        return Optional.empty();
    }

    private static String group(String key) {
        int dot = key.indexOf('.');
        return dot < 0 ? key : key.substring(0, dot);
    }

    private static Map<String, String> subtitles(String subtitle, Map<String, Map<String, String>> languages) {
        Map<String, String> found = new LinkedHashMap<>();
        if (subtitle.isEmpty()) return found;
        languages.forEach((language, translations) -> {
            String text = translations.get(subtitle);
            if (text != null && !text.isBlank()) found.put(language, text);
        });
        return found;
    }
}
