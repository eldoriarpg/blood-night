/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.assets.ItemAsset;
import de.eldoria.bloodnight.web.api.assets.ItemIndex;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the items of a Minecraft client jar: every item definition in {@code assets/minecraft/items}, the icon its
 * model shows and its names.
 * <p>
 * An item definition can switch between models (by component, range or condition); the first plain model in it is
 * the icon, else the base model of the first special model (chests, banners, heads, shields). That model's textures
 * are merged along its parents, the child's winning, a texture given as {@code sprite} object counting as that
 * sprite; the icon is {@code layer0} for
 * flat items, else a face of the block ({@code all}, {@code side}, {@code top}, {@code front}, {@code particle}, else
 * the first texture), following {@code #} references. Only textures under {@code item/} and {@code block/} that the jar
 * holds are used. Names come from {@code item.minecraft.<id>}, else {@code block.minecraft.<id>}, of each language;
 * the names of registry entries from the language keys in {@link #REGISTRIES}, e.g. {@code entity.minecraft.<id>}.
 */
final class ItemAssetExtractor {
    private static final String ITEMS = "assets/minecraft/items/";
    private static final Map<String, String> REGISTRIES = Map.of(
            "enchantment", "enchantment.minecraft.",
            "entity_type", "entity.minecraft.",
            "effect", "effect.minecraft.",
            "biome", "biome.minecraft.",
            "attribute", "attribute.name.",
            "potion", "item.minecraft.potion.effect.",
            "game_rule", "gamerule.");
    private static final List<String> FACES = List.of("layer0", "all", "side", "top", "front", "particle");
    private static final int MAX_DEPTH = 16;

    private ItemAssetExtractor() {}

    /**
     * What a client jar yields.
     *
     * @param index    the items
     * @param textures jar entry of each texture the index names, e.g. {@code item/diamond_sword} to
     *                 {@code assets/minecraft/textures/item/diamond_sword.png}
     */
    record Extraction(ItemIndex index, Map<String, String> textures) {}

    /**
     * @param version   the Minecraft version
     * @param jar       its client jar
     * @param languages translations by language code, e.g. {@code de_de}; {@code en_us} is read from the jar
     * @return the items and the textures to copy out of the jar
     */
    static Extraction extract(String version, ZipFile jar, Map<String, Map<String, String>> languages) {
        Map<String, Map<String, String>> names = new LinkedHashMap<>();
        names.put("en_us", language(jar, "assets/minecraft/lang/en_us.json"));
        names.putAll(languages);
        Map<String, ItemAsset> items = new TreeMap<>();
        Map<String, String> textures = new TreeMap<>();
        jar.stream()
                .map(ZipEntry::getName)
                .filter(name -> name.startsWith(ITEMS) && name.endsWith(".json"))
                .forEach(name -> {
                    String id = name.substring(ITEMS.length(), name.length() - ".json".length());
                    if (id.contains("/")) return;
                    JsonNode definition = read(jar, name).path("model");
                    String texture = firstModel(definition, "minecraft:model", "model")
                            .or(() -> firstModel(definition, "minecraft:special", "base"))
                            .flatMap(model -> icon(jar, model))
                            .orElse(null);
                    if (texture != null) textures.put(texture, "assets/minecraft/textures/" + texture + ".png");
                    items.put("minecraft:" + id, new ItemAsset(texture, names(id, names)));
                });
        return new Extraction(new ItemIndex(version, items, registryNames(names)), textures);
    }

    /**
     * @param json a language file
     * @return its translations
     */
    static Map<String, String> language(byte[] json) {
        Map<String, String> translations = new HashMap<>();
        EditorApp.MAPPER
                .readTree(json)
                .properties()
                .forEach(entry ->
                        translations.put(entry.getKey(), entry.getValue().asString("")));
        return translations;
    }

    static Map<String, String> language(ZipFile jar, String entry) {
        ZipEntry found = jar.getEntry(entry);
        if (found == null) return Map.of();
        try (InputStream in = jar.getInputStream(found)) {
            return language(in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Map<String, Map<String, String>>> registryNames(
            Map<String, Map<String, String>> languages) {
        Map<String, Map<String, Map<String, String>>> registries = new TreeMap<>();
        REGISTRIES.forEach((registry, prefix) -> registries.put(registry, entryNames(prefix, languages)));
        return registries;
    }

    private static Map<String, Map<String, String>> entryNames(
            String prefix, Map<String, Map<String, String>> languages) {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        languages.forEach((language, translations) -> translations.forEach((key, name) -> {
            if (!key.startsWith(prefix)) return;
            String id = key.substring(prefix.length());
            if (id.contains(".")) return;
            entries.computeIfAbsent("minecraft:" + id, ignored -> new TreeMap<>())
                    .put(language, name);
        }));
        return entries;
    }

    private static Map<String, String> names(String id, Map<String, Map<String, String>> languages) {
        Map<String, String> names = new TreeMap<>();
        languages.forEach((language, translations) -> {
            String name = translations.getOrDefault("item.minecraft." + id, translations.get("block.minecraft." + id));
            if (name != null) names.put(language, name);
        });
        return names;
    }

    private static Optional<String> firstModel(JsonNode node, String type, String field) {
        if (node.isObject()
                && type.equals(node.path("type").asString(""))
                && node.path(field).isString()) {
            return Optional.of(node.path(field).asString());
        }
        for (JsonNode child : node.isObject() ? node.values() : node) {
            if (!child.isContainer()) continue;
            Optional<String> found = firstModel(child, type, field);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    private static Optional<String> icon(ZipFile jar, String model) {
        Map<String, String> textures = new HashMap<>();
        String current = model;
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            JsonNode json = readOrNull(jar, "assets/minecraft/models/" + path(current) + ".json");
            if (json == null) break;
            json.path("textures")
                    .properties()
                    .forEach(entry -> textures.putIfAbsent(entry.getKey(), sprite(entry.getValue())));
            current = json.path("parent").isString() ? json.path("parent").asString() : null;
        }
        List<String> order = FACES.stream().filter(textures::containsKey).toList();
        for (String key : order.isEmpty() ? List.copyOf(textures.keySet()) : order) {
            String texture = follow(textures, textures.get(key));
            if (texture != null
                    && (texture.startsWith("item/") || texture.startsWith("block/"))
                    && jar.getEntry("assets/minecraft/textures/" + texture + ".png") != null) {
                return Optional.of(texture);
            }
        }
        return Optional.empty();
    }

    private static String sprite(JsonNode texture) {
        return texture.isObject() ? texture.path("sprite").asString("") : texture.asString("");
    }

    private static @Nullable String follow(Map<String, String> textures, @Nullable String value) {
        String current = value;
        for (int depth = 0; current != null && current.startsWith("#") && depth < MAX_DEPTH; depth++) {
            current = textures.get(current.substring(1));
        }
        return current == null || current.startsWith("#") ? null : path(current);
    }

    private static String path(String reference) {
        return reference.startsWith("minecraft:") ? reference.substring("minecraft:".length()) : reference;
    }

    private static JsonNode read(ZipFile jar, String entry) {
        JsonNode json = readOrNull(jar, entry);
        return json == null ? EditorApp.MAPPER.createObjectNode() : json;
    }

    private static @Nullable JsonNode readOrNull(ZipFile jar, String entry) {
        ZipEntry found = jar.getEntry(entry);
        if (found == null) return null;
        try (InputStream in = jar.getInputStream(found)) {
            return EditorApp.MAPPER.readTree(in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
