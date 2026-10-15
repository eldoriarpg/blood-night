/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BuiltInStoreTest {
    private static final Path ROOT = Path.of("..");

    @TempDir
    Path out;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void theCommittedStoreIsCurrent() throws IOException {
        StoreGenerator.generate(
                ROOT.resolve(StoreGeneratorCli.DESCRIPTIONS),
                out.resolve("store"),
                out.resolve("lang"),
                ROOT.resolve(StoreGeneratorCli.DATAPACK),
                ROOT.resolve(StoreGeneratorCli.TEXTS));

        assertSame(out.resolve("store"), ROOT.resolve(StoreGeneratorCli.STORE));
        assertSame(out.resolve("lang"), ROOT.resolve(StoreGeneratorCli.LANG));
    }

    @Test
    void everyMobHasNamesAndAPack() {
        Store store = BuiltInStore.load();

        assertEquals(35, store.entries().size());
        for (StoreEntry entry : store.entries()) {
            assertFalse(store.resolve(entry.pack()).isEmpty(), entry.id());
            assertFalse(entry.mobPackage().names().getOrDefault("de", "").isBlank(), entry.id());
            assertFalse(entry.mobPackage().descriptions().getOrDefault("en", "").isBlank(), entry.id());
        }
    }

    private static void assertSame(Path generated, Path committed) throws IOException {
        long count;
        try (Stream<Path> files = Files.list(generated)) {
            count = files.count();
        }
        try (Stream<Path> files = Files.list(generated)) {
            for (Path file : files.toList()) {
                Path other = committed.resolve(file.getFileName());
                assertEquals(
                        Files.readString(file),
                        Files.exists(other) ? Files.readString(other) : "",
                        "Stale %s, run ./gradlew :core:generateStore".formatted(other));
            }
        }
        try (Stream<Path> files = Files.list(committed)) {
            assertEquals(count, files.count(), "Extra files in " + committed);
        }
    }
}
