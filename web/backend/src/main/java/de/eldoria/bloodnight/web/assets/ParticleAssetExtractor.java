/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.assets;

import de.eldoria.bloodnight.web.EditorApp;
import de.eldoria.bloodnight.web.api.assets.ParticleIndex;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the particles of a client jar: each {@code assets/minecraft/particles/<id>.json} lists the sprites the game
 * draws the particle with, from {@code textures/particle/}. Sprites the jar does not hold are left out.
 */
final class ParticleAssetExtractor {
    private static final String PARTICLES = "assets/minecraft/particles/";
    private static final String TEXTURES = "assets/minecraft/textures/";

    private ParticleAssetExtractor() {}

    /**
     * What a client jar yields.
     *
     * @param index    the sprites of each particle
     * @param textures jar entry of each sprite the index names, e.g. {@code particle/flame} to
     *                 {@code assets/minecraft/textures/particle/flame.png}
     */
    record Extraction(ParticleIndex index, Map<String, String> textures) {}

    /**
     * @param version the Minecraft version
     * @param jar     its client jar
     * @return the particles and the sprites to copy out of the jar
     */
    static Extraction extract(String version, ZipFile jar) {
        Map<String, List<String>> particles = new TreeMap<>();
        Map<String, String> textures = new TreeMap<>();
        jar.stream()
                .map(ZipEntry::getName)
                .filter(name -> name.startsWith(PARTICLES) && name.endsWith(".json"))
                .forEach(name -> {
                    String id = name.substring(PARTICLES.length(), name.length() - ".json".length());
                    if (id.contains("/")) return;
                    List<String> sprites = new ArrayList<>();
                    for (JsonNode sprite : read(jar, name).path("textures")) {
                        String texture = "particle/" + sprite.asString("").replaceFirst("^minecraft:", "");
                        String entry = TEXTURES + texture + ".png";
                        if (jar.getEntry(entry) == null) continue;
                        sprites.add(texture);
                        textures.put(texture, entry);
                    }
                    particles.put("minecraft:" + id, sprites);
                });
        return new Extraction(new ParticleIndex(version, particles), textures);
    }

    private static JsonNode read(ZipFile jar, String name) {
        try (InputStream in = jar.getInputStream(jar.getEntry(name))) {
            return EditorApp.MAPPER.readTree(in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
