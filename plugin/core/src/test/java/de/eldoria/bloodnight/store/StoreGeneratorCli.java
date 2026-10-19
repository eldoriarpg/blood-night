/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.store.build.StoreGenerator;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes the plugin's own store for {@code ./gradlew :core:generateStore}, run from the project root.
 */
public final class StoreGeneratorCli {
    public static final Path DESCRIPTIONS = Path.of("plugin/core/src/main/store/bloodnight");
    public static final Path STORE = Path.of("plugin/core/src/main/resources/store/bloodnight");
    public static final Path LANG = Path.of("plugin/resourcepack/assets/bloodnight/lang");
    public static final Path DATAPACK = Path.of("plugin/core/src/main/resources/datapack");
    public static final Path TEXTS = Path.of("plugin/core/src/main/lang");

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
