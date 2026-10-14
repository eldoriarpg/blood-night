/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.defaults.DefaultMobs;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Builds the plugin's own store at build time: the mobs of {@link DefaultMobs}, each joined with its hand-written
 * description ({@code <id>.json}: pack, names, descriptions, requirements) into a {@link MobPackage}, and the
 * packs of {@code packs.json}. The result is committed in {@code src/main/resources/store/bloodnight}. Also writes the mob names for the resource pack ({@code <language>_<region>.json}).
 */
public final class StoreGenerator {
    private static final Map<String, String> PACK_LANGUAGES = Map.of("en", "en_us", "de", "de_de");

    private StoreGenerator() {}

    /**
     * Needs a server for the registries the editor's mapper reads, e.g. a mocked one.
     *
     * @param descriptions the hand-written descriptions and {@code packs.json}
     * @param store        where the store is written
     * @param lang         where the resource pack's name files are written
     * @throws IllegalStateException when a default mob has no description or a description no mob
     */
    public static void generate(Path descriptions, Path store, Path lang) throws IOException {
        List<StorePack> packs = EditorPayload.MAPPER.readValue(
                Files.readString(descriptions.resolve("packs.json")), new TypeReference<List<StorePack>>() {});
        Map<String, Description> described = new TreeMap<>();
        try (var files = Files.list(descriptions)) {
            for (Path file : files.toList()) {
                String name = String.valueOf(file.getFileName());
                if (!name.endsWith(".json") || name.equals("packs.json")) continue;
                described.put(
                        name.substring(0, name.length() - ".json".length()),
                        EditorPayload.MAPPER.readValue(Files.readString(file), Description.class));
            }
        }
        List<CustomMob> mobs = DefaultMobs.create(
                BloodNightPlatform.create(List.of()),
                id -> Objects.requireNonNullElse(
                        described(described, id).names().get("en"), id));
        if (mobs.size() != described.size()) {
            throw new IllegalStateException(
                    "%d descriptions for %d default mobs".formatted(described.size(), mobs.size()));
        }
        List<MobPackage> packages = new ArrayList<>();
        for (CustomMob mob : mobs) {
            Description description = described(described, mob.id());
            packages.add(new MobPackage(
                    StoreFiles.FORMAT,
                    mob.id(),
                    description.pack(),
                    description.names(),
                    description.descriptions(),
                    description.requires(),
                    mob));
        }
        StoreFiles.write(
                store,
                new StoreIndex(
                        StoreFiles.FORMAT,
                        BuiltInStore.ID,
                        packs,
                        packages.stream().map(MobPackage::id).toList()),
                packages);
        writeLang(lang, packages);
    }

    private static Description described(Map<String, Description> described, String id) {
        Description description = described.get(id);
        if (description == null) throw new IllegalStateException("Default mob %s has no description".formatted(id));
        return description;
    }

    private static void writeLang(Path lang, List<MobPackage> packages) throws IOException {
        Files.createDirectories(lang);
        for (var language : PACK_LANGUAGES.entrySet()) {
            Map<String, String> names = new LinkedHashMap<>();
            for (MobPackage mobPackage : packages) {
                String name = mobPackage.names().get(language.getKey());
                if (name != null) names.put("bloodnight.mob." + mobPackage.id(), name);
            }
            Files.writeString(
                    lang.resolve(language.getValue() + ".json"),
                    EditorPayload.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(names) + "\n");
        }
    }

    /**
     * The hand-written part of a mob package.
     *
     * @param pack         the pack the mob belongs to
     * @param names        its name by language
     * @param descriptions what it does, by language
     * @param requires     plugins it needs
     */
    record Description(
            String pack, Map<String, String> names, Map<String, String> descriptions, List<String> requires) {}
}
