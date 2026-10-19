/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.behaviour;

import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.GraphJson;
import de.eldoria.graph.model.Macro;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Compiled behaviours on disk, so a restart or reload only compiles graphs that changed.
 * <p>
 * Entries are keyed by a hash over the plugin version, the graph and the macro library: a new plugin version can
 * change the nodes' code, and a changed macro changes every graph using it. One file per entry, holding the class
 * name, the source for debugging, the class files and the warnings.
 */
public final class BytecodeCache {
    private final Path directory;

    /**
     * @param directory where entries are stored; created when needed
     */
    public BytecodeCache(Path directory) {
        this.directory = directory;
    }

    /**
     * A cached compilation.
     *
     * @param name     binary name of the generated class
     * @param source   its source
     * @param classes  class files by binary name, base64
     * @param warnings warnings of the compilation
     */
    public record Entry(String name, String source, Map<String, String> classes, List<GraphProblem> warnings) {
        static Entry of(String name, String source, Map<String, byte[]> bytecode, List<GraphProblem> warnings) {
            Map<String, String> encoded = new HashMap<>();
            bytecode.forEach((className, bytes) ->
                    encoded.put(className, Base64.getEncoder().encodeToString(bytes)));
            return new Entry(name, source, encoded, warnings);
        }

        Map<String, byte[]> bytecode() {
            Map<String, byte[]> decoded = new HashMap<>();
            classes.forEach((className, text) ->
                    decoded.put(className, Base64.getDecoder().decode(text)));
            return decoded;
        }
    }

    /**
     * The class name is part of the key: two mobs with identical graphs compile to classes of different names, and
     * would otherwise take turns overwriting one shared entry.
     *
     * @param version   the plugin version
     * @param className binary name of the generated class
     * @param graph     the behaviour graph
     * @param macros    the macro library
     * @param bindings  the graph-wide types it is compiled with, e.g. its mob's entity type
     * @return the cache key
     */
    public static String key(
            String version, String className, Graph graph, List<Macro> macros, Map<String, PortType> bindings) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(version.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(className.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(GraphJson.write(graph).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(GraphJson.mapper().writeValueAsString(macros).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            new TreeMap<>(bindings)
                    .forEach((variable, type) ->
                            digest.update((variable + "=" + type.id() + ";").getBytes(StandardCharsets.UTF_8)));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }

    /**
     * @param key a cache key
     * @return the entry, if cached and readable; unreadable entries count as missing
     */
    public Optional<Entry> read(String key) {
        Path file = file(key);
        if (!Files.isRegularFile(file)) return Optional.empty();
        try {
            return Optional.of(GraphJson.mapper().readValue(Files.readString(file), Entry.class));
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * @param key   a cache key
     * @param entry the compilation
     */
    public void write(String key, Entry entry) {
        try {
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, key, ".tmp");
            Files.writeString(temporary, GraphJson.mapper().writeValueAsString(entry));
            Files.move(temporary, file(key), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Deletes every entry not in the given set.
     *
     * @param keys keys still in use
     */
    public void retain(Set<String> keys) {
        if (!Files.isDirectory(directory)) return;
        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.toList()) {
                Path fileName = file.getFileName();
                if (fileName == null) continue;
                String name = fileName.toString();
                if (!name.endsWith(".json") || !keys.contains(name.substring(0, name.length() - ".json".length())))
                    Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path file(String key) {
        return directory.resolve(key + ".json");
    }
}
