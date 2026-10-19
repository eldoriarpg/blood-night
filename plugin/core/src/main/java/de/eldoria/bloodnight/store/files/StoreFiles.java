/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store.files;

import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.bloodnight.store.Store;
import de.eldoria.bloodnight.store.StoreEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads and writes stores in BloodNight's own format: an {@code index.json} ({@link StoreIndex}) and one
 * {@code <id>.json} per mob ({@link MobPackage}) in the same place. JSON as the web editor writes it.
 */
public final class StoreFiles {
    /**
     * The format this version of BloodNight reads and writes.
     */
    public static final int FORMAT = 1;

    private StoreFiles() {}

    /**
     * Opens a file of a store by its name, e.g. {@code index.json}.
     */
    @FunctionalInterface
    public interface Source {
        /**
         * @return the file's content
         * @throws IOException when it cannot be read
         */
        InputStream open(String name) throws IOException;
    }

    /**
     * @return the store
     * @throws UncheckedIOException when a file cannot be read
     * @throws IllegalArgumentException when a file has another format or a package names another id
     */
    public static Store read(Source source) {
        StoreIndex index = parse(source, "index.json", StoreIndex.class);
        if (index.format() != FORMAT) {
            throw new IllegalArgumentException(
                    "Store %s has format %d, not %d".formatted(index.id(), index.format(), FORMAT));
        }
        List<StoreEntry> entries = index.entries().stream()
                .map(id -> parse(source, id + ".json", MobPackage.class))
                .peek(StoreFiles::check)
                .map(StoreEntry::of)
                .toList();
        return new Store(index.id(), index.packs(), entries);
    }

    /**
     * Writes a store to a directory, one file per mob and the index.
     */
    public static void write(Path directory, StoreIndex index, List<MobPackage> packages) throws IOException {
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("index.json"), json(index));
        for (MobPackage mobPackage : packages) {
            Files.writeString(directory.resolve(mobPackage.id() + ".json"), json(mobPackage));
        }
    }

    private static void check(MobPackage mobPackage) {
        if (mobPackage.format() != FORMAT) {
            throw new IllegalArgumentException(
                    "Mob package %s has format %d, not %d".formatted(mobPackage.id(), mobPackage.format(), FORMAT));
        }
        if (!mobPackage.id().equals(mobPackage.mob().id())) {
            throw new IllegalArgumentException("Mob package %s holds the mob %s"
                    .formatted(mobPackage.id(), mobPackage.mob().id()));
        }
    }

    private static <T> T parse(Source source, String name, Class<T> type) {
        try (InputStream stream = source.open(name)) {
            return EditorPayload.MAPPER.readValue(stream, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read store file " + name, e);
        }
    }

    private static String json(Object value) {
        return EditorPayload.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value) + "\n";
    }
}
