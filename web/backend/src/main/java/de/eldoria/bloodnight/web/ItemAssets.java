/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Minecraft's item icons and names per version, fetched from Mojang once and kept in a cache directory.
 * <p>
 * The first request for a version reads Mojang's version manifest, downloads that version's client jar and its German
 * language file (both checked against the sha1 Mojang publishes), extracts the items with {@link ItemAssetExtractor}
 * and writes {@code <cache>/<version>/items.json} and the icons below {@code textures/}; the version's directory
 * appears in one move once complete. Requests arriving meanwhile wait for the same download. A failed download is
 * not remembered, so the next request tries again. Nothing from Mojang is stored outside the cache.
 */
final class ItemAssets {
    private static final Logger log = LoggerFactory.getLogger(ItemAssets.class);
    private static final Pattern VERSION = Pattern.compile("[0-9a-z][0-9a-z._-]{0,63}");
    private static final Pattern NAME = Pattern.compile("[a-z0-9_]{1,128}\\.png");
    private static final String GERMAN = "de_de";
    private final Path cache;
    private final HttpClient http;
    private final URI manifest;
    private final URI resources;
    private final Map<String, CompletableFuture<Path>> building = new ConcurrentHashMap<>();

    /**
     * Thrown when Mojang does not know a version.
     */
    static final class UnknownVersion extends Exception {
        UnknownVersion(String version) {
            super("Mojang knows no version " + version);
        }
    }

    /**
     * Thrown when the assets cannot be fetched right now.
     */
    static final class Unavailable extends Exception {
        Unavailable(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * @param cache     the cache directory
     * @param http      the client for Mojang's servers
     * @param manifest  Mojang's version manifest
     * @param resources the base of Mojang's asset objects, the hash's first two characters and the hash appended
     */
    ItemAssets(Path cache, HttpClient http, URI manifest, URI resources) {
        this.cache = cache;
        this.http = http;
        this.manifest = manifest;
        this.resources = resources;
    }

    /**
     * @param cache the cache directory
     * @return assets fetched from Mojang's servers
     */
    static ItemAssets mojang(Path cache) {
        return new ItemAssets(
                cache,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(20))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"),
                URI.create("https://resources.download.minecraft.net/"));
    }

    /**
     * @param version a Minecraft version, e.g. {@code 26.3}
     * @return the version's {@code items.json}, fetched first if needed
     * @throws UnknownVersion when the version is malformed or Mojang does not know it
     * @throws Unavailable    when it cannot be fetched now
     */
    Path items(String version) throws UnknownVersion, Unavailable {
        if (!VERSION.matcher(version).matches()) throw new UnknownVersion(version);
        Path index = cache.resolve(version).resolve("items.json");
        if (Files.isRegularFile(index)) return index;
        CompletableFuture<Path> build = building.computeIfAbsent(
                version,
                key -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return build(key);
                    } catch (UnknownVersion e) {
                        throw new IllegalStateException(e);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
        try {
            return build.get().resolve("items.json");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Unavailable("Interrupted while fetching " + version, e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IllegalStateException state
                    && state.getCause() instanceof UnknownVersion unknown) throw unknown;
            throw new Unavailable("Could not fetch the assets of " + version, e.getCause());
        } finally {
            building.remove(version, build);
        }
    }

    /**
     * @param version a version whose items were fetched
     * @param folder  {@code item} or {@code block}
     * @param name    the texture's file name, e.g. {@code diamond_sword.png}
     * @return the icon, if the version's assets hold it
     */
    Optional<Path> texture(String version, String folder, String name) {
        if (!VERSION.matcher(version).matches() || !NAME.matcher(name).matches()) return Optional.empty();
        if (!folder.equals("item") && !folder.equals("block")) return Optional.empty();
        Path texture =
                cache.resolve(version).resolve("textures").resolve(folder).resolve(name);
        return Files.isRegularFile(texture) ? Optional.of(texture) : Optional.empty();
    }

    private Path build(String version) throws IOException, UnknownVersion {
        Path target = cache.resolve(version);
        if (Files.isRegularFile(target.resolve("items.json"))) return target;
        log.info("Fetching the item assets of Minecraft {}", version);
        JsonNode entry = null;
        for (JsonNode candidate : json(manifest).path("versions")) {
            if (candidate.path("id").asString("").equals(version)) entry = candidate;
        }
        if (entry == null) throw new UnknownVersion(version);
        JsonNode meta = EditorApp.MAPPER.readTree(verified(
                URI.create(entry.path("url").asString()), entry.path("sha1").asString()));
        JsonNode client = meta.path("downloads").path("client");
        JsonNode assetIndex = meta.path("assetIndex");
        JsonNode index = EditorApp.MAPPER.readTree(verified(
                URI.create(assetIndex.path("url").asString()),
                assetIndex.path("sha1").asString()));
        Map<String, Map<String, String>> languages = Map.of();
        JsonNode german = index.path("objects").path("minecraft/lang/" + GERMAN + ".json");
        if (german.path("hash").isString()) {
            String hash = german.path("hash").asString();
            languages = Map.of(
                    GERMAN,
                    ItemAssetExtractor.language(verified(resources.resolve(hash.substring(0, 2) + "/" + hash), hash)));
        }
        Files.createDirectories(cache);
        Path jar = Files.createTempFile(cache, "client", ".jar");
        Path staging = Files.createTempDirectory(cache, version + ".");
        try {
            Files.write(
                    jar,
                    verified(
                            URI.create(client.path("url").asString()),
                            client.path("sha1").asString()));
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                ItemAssetExtractor.Extraction extraction = ItemAssetExtractor.extract(version, zip, languages);
                for (var texture : extraction.textures().entrySet()) {
                    Path file = staging.resolve("textures").resolve(texture.getKey() + ".png");
                    Files.createDirectories(Objects.requireNonNull(file.getParent()));
                    ZipEntry source = zip.getEntry(texture.getValue());
                    try (InputStream in = zip.getInputStream(source)) {
                        Files.copy(in, file);
                    }
                }
                Files.write(staging.resolve("items.json"), EditorApp.MAPPER.writeValueAsBytes(extraction.index()));
                log.info(
                        "Cached {} items and {} icons of Minecraft {}",
                        extraction.index().items().size(),
                        extraction.textures().size(),
                        version);
            }
            Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE);
            return target;
        } finally {
            Files.deleteIfExists(jar);
            if (Files.exists(staging)) delete(staging);
        }
    }

    private JsonNode json(URI uri) throws IOException {
        return EditorApp.MAPPER.readTree(fetch(uri));
    }

    private byte[] verified(URI uri, String sha1) throws IOException {
        byte[] body = fetch(uri);
        String actual = sha1(body);
        if (!actual.equalsIgnoreCase(sha1))
            throw new IOException("%s has sha1 %s, not %s".formatted(uri, actual, sha1));
        return body;
    }

    private byte[] fetch(URI uri) throws IOException {
        try {
            HttpResponse<byte[]> response = http.send(
                    HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(5)).build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200)
                throw new IOException("%s answered %d".formatted(uri, response.statusCode()));
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while fetching " + uri, e);
        }
    }

    private static String sha1(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Every Java runtime has SHA-1", e);
        }
    }

    private static void delete(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
}
