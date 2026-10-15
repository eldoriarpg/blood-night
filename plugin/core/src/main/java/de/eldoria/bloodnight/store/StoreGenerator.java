/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.loot.MobLoot;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.defaults.DefaultMobs;
import de.eldoria.bloodnight.mob.meta.MobName;
import de.eldoria.bloodnight.web.api.Language;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Builds the plugin's own store at build time: the mobs of {@link DefaultMobs}, each joined with its hand-written
 * description ({@code <id>.json}: pack, names, descriptions, requirements) into a {@link MobPackage}, and the
 * packs of {@code packs.json}. The result is committed in {@code src/main/resources/store/bloodnight}. Also writes the
 * resource pack's language files ({@code <language>_<region>.json}): the mob names, and the data pack's texts (see
 * {@link PackTexts}).
 */
public final class StoreGenerator {
    private static final Map<String, String> PACK_LANGUAGES = Arrays.stream(Language.values())
            .collect(Collectors.toMap(
                    Language::language, Language::minecraft, (first, second) -> first, LinkedHashMap::new));

    private StoreGenerator() {}

    /**
     * Needs a server for the registries the editor's mapper reads, e.g. a mocked one.
     *
     * @param descriptions the hand-written descriptions and {@code packs.json}
     * @param store        where the store is written
     * @param lang         where the resource pack's language files are written
     * @param datapack     the plugin's data pack, whose texts go into the language files
     * @param texts        the data pack's texts in languages other than English
     * @throws IllegalStateException when a default mob has no description or a description no mob, or a language
     *     file does not cover the data pack's texts
     */
    public static void generate(Path descriptions, Path store, Path lang, Path datapack, Path texts)
            throws IOException {
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
        for (CustomMob plain : mobs) {
            Description description = described(described, plain.id());
            MobName name = new MobName(plain.attributes().name().style(), description.names());
            StoreLoot loot = Objects.requireNonNullElse(description.loot(), StoreLoot.NONE);
            CustomMob mob = plain.withAttributes(plain.attributes().withName(name))
                    .withLoot(new MobLoot(
                            List.copyOf(loot.tables().keySet()),
                            loot.slots(),
                            true,
                            true,
                            plain.loot().vanillaLoot(),
                            plain.loot().experience()));
            packages.add(new MobPackage(
                    StoreFiles.FORMAT,
                    mob.id(),
                    description.pack(),
                    description.names(),
                    description.descriptions(),
                    description.requires(),
                    mob,
                    loot.tables()));
        }
        StoreFiles.write(
                store,
                new StoreIndex(
                        StoreFiles.FORMAT,
                        BuiltInStore.ID,
                        packs,
                        packages.stream().map(MobPackage::id).toList()),
                packages);
        writeLang(lang, packages, datapack, texts);
    }

    private static Description described(Map<String, Description> described, String id) {
        Description description = described.get(id);
        if (description == null) throw new IllegalStateException("Default mob %s has no description".formatted(id));
        return description;
    }

    private static void writeLang(Path lang, List<MobPackage> packages, Path datapack, Path texts) throws IOException {
        Files.createDirectories(lang);
        Map<String, String> english = PackTexts.english(datapack);
        for (var language : PACK_LANGUAGES.entrySet()) {
            Map<String, String> names = new LinkedHashMap<>();
            for (MobPackage mobPackage : packages) {
                String name = mobPackage.names().get(language.getKey());
                if (name != null) names.put("bloodnight.mob." + mobPackage.id(), name);
            }
            names.putAll(
                    language.getKey().equals("en")
                            ? english
                            : PackTexts.translated(texts, language.getKey(), english.keySet()));
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
            String pack,
            Map<String, String> names,
            Map<String, String> descriptions,
            List<String> requires,
            @Nullable StoreLoot loot) {}

    /**
     * The loot a store mob brings: its own tables by id, and how many entries a kill drops.
     *
     * @param tables the tables, assigned to the mob in this order
     * @param slots  weights by number of drops; none leaves it to the world and the server
     */
    record StoreLoot(
            Map<String, LootTable> tables, @Nullable Map<Integer, Integer> slots) {
        static final StoreLoot NONE = new StoreLoot(Map.of(), null);

        StoreLoot {
            tables = tables == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(tables));
        }
    }
}
