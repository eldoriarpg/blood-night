/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.legacy;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The files the old BloodNight (0.12 and older) left in the plugin's folder, which this plugin shares.
 * <p>
 * The old {@code config.yml} has {@code generalSettings} (layout 2, worlds in {@code worldSettings/<world>.yml}) or
 * {@code worldSettings} (layout 1, every world inside it). On first start they are moved into {@code legacy/}, so this
 * plugin never reads them as its own. They are read as plain YAML: the old plugin's type tags ({@code ==}) are kept as
 * keys and ignored, so the old plugin is not needed.
 */
public final class LegacyFolder {
    /**
     * Where the old files are moved to, inside the plugin's folder.
     */
    public static final String FOLDER = "legacy";

    private static final YAMLMapper YAML = YAMLMapper.builder().build();
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final List<String> LEFTOVERS = List.of("config.yml", "worldSettings", "messages", "backup");

    private LegacyFolder() {}

    /**
     * Moves the old plugin's files out of the way, when there are any.
     *
     * @param dataFolder the plugin's folder
     * @return where they went; empty when there were none
     */
    public static Optional<Path> moveAside(Path dataFolder) throws IOException {
        if (!isLegacy(dataFolder)) return Optional.empty();
        Path target = dataFolder.resolve(FOLDER);
        for (int i = 2; Files.exists(target); i++) target = dataFolder.resolve(FOLDER + "-" + i);
        Files.createDirectories(target);
        for (String name : LEFTOVERS) {
            Path file = dataFolder.resolve(name);
            if (Files.exists(file)) Files.move(file, target.resolve(name));
        }
        return Optional.of(target);
    }

    /**
     * @return whether the folder holds the old plugin's settings
     */
    static boolean isLegacy(Path dataFolder) {
        if (Files.isDirectory(dataFolder.resolve("worldSettings"))) return true;
        Path config = dataFolder.resolve("config.yml");
        if (!Files.exists(config)) return false;
        try {
            Map<String, Object> top = yaml(config);
            return top.containsKey("generalSettings") || top.containsKey("worldSettings");
        } catch (IOException | JacksonException e) {
            return false;
        }
    }

    /**
     * @param folder a folder with the old plugin's files, e.g. {@code plugins/BloodNight/legacy}
     * @return what they hold
     * @throws IOException when the old {@code config.yml} is missing or cannot be read
     */
    public static Legacy read(Path folder) throws IOException {
        Map<String, Object> config = yaml(folder.resolve("config.yml"));
        Map<String, Object> general = Legacy.map(config.get("generalSettings"));
        Map<String, Map<String, Object>> worlds = new LinkedHashMap<>();
        Object inline = config.get("worldSettings");
        if (inline instanceof List<?> list) {
            for (Object world : list) add(worlds, Legacy.map(world));
        }
        Path perWorld = folder.resolve("worldSettings");
        if (Files.isDirectory(perWorld)) {
            List<Path> files = new ArrayList<>();
            try (Stream<Path> listing = Files.list(perWorld)) {
                listing.filter(file -> file.toString().endsWith(".yml"))
                        .sorted()
                        .forEach(files::add);
            }
            for (Path file : files) add(worlds, Legacy.map(yaml(file).get("settings")));
        }
        return new Legacy(general, worlds);
    }

    private static void add(Map<String, Map<String, Object>> worlds, Map<String, Object> world) {
        Object name = world.get("world");
        if (name != null) worlds.put(name.toString(), world);
    }

    private static Map<String, Object> yaml(Path file) throws IOException {
        try {
            Map<String, Object> content = YAML.readValue(Files.readString(file), MAP);
            return content == null ? Map.of() : content;
        } catch (JacksonException e) {
            throw new IOException("Cannot read " + file.getFileName() + ": " + e.getOriginalMessage(), e);
        }
    }
}
