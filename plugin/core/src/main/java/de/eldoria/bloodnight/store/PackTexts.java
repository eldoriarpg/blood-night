/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.editor.EditorPayload;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * The texts of the plugin's data pack, for the resource pack's language files. Every {@code translate} component in
 * the data pack with a {@code fallback} gives a key and its English text, so English needs no file of its own. Other
 * languages come from hand-written files, {@code <language>.json} from key to text, which must cover exactly the keys
 * the data pack uses.
 */
final class PackTexts {
    private PackTexts() {}

    /**
     * @param datapack the data pack's folder
     * @return the English text of every key, sorted by key
     */
    static Map<String, String> english(Path datapack) throws IOException {
        Map<String, String> texts = new TreeMap<>();
        try (Stream<Path> files = Files.walk(datapack)) {
            for (Path file :
                    files.filter(path -> path.toString().endsWith(".json")).toList()) {
                collect(EditorPayload.MAPPER.readTree(Files.readString(file)), texts);
            }
        }
        return texts;
    }

    /**
     * @param texts    the folder of the hand-written language files
     * @param language a language, e.g. {@code de}
     * @param keys     the keys the data pack uses
     * @return the language's text of every key, sorted by key
     * @throws IllegalStateException when the file lacks a key or has one the data pack does not use
     */
    static Map<String, String> translated(Path texts, String language, Set<String> keys) throws IOException {
        Map<String, String> translated = new TreeMap<>(EditorPayload.MAPPER.readValue(
                Files.readString(texts.resolve(language + ".json")), new TypeReference<Map<String, String>>() {}));
        if (!translated.keySet().equals(keys)) {
            Set<String> missing = new TreeSet<>(keys);
            missing.removeAll(translated.keySet());
            Set<String> unused = new TreeSet<>(translated.keySet());
            unused.removeAll(keys);
            throw new IllegalStateException("%s.json: missing %s, unused %s".formatted(language, missing, unused));
        }
        return translated;
    }

    private static void collect(JsonNode node, Map<String, String> texts) {
        if (node.isObject()
                && node.path("translate").isString()
                && node.path("fallback").isString()) {
            texts.put(node.path("translate").asString(), node.path("fallback").asString());
        }
        for (JsonNode child : node) collect(child, texts);
    }
}
