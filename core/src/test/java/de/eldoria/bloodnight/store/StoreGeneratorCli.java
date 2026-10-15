/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes the plugin's own store for {@code ./gradlew :core:generateStore}, run from the project root.
 */
public final class StoreGeneratorCli {
    static final Path DESCRIPTIONS = Path.of("core/src/main/store/bloodnight");
    static final Path STORE = Path.of("core/src/main/resources/store/bloodnight");
    static final Path LANG = Path.of("resourcepack/assets/bloodnight/lang");
    static final Path DATAPACK = Path.of("core/src/main/resources/datapack");
    static final Path TEXTS = Path.of("core/src/main/lang");

    private StoreGeneratorCli() {}

    public static void main(String[] args) throws IOException {
        MockBukkit.mock();
        try {
            StoreGenerator.generate(DESCRIPTIONS, STORE, LANG, DATAPACK, TEXTS);
        } finally {
            MockBukkit.unmock();
        }
        System.out.println("Wrote " + STORE.toAbsolutePath() + " and " + LANG.toAbsolutePath());
    }
}
