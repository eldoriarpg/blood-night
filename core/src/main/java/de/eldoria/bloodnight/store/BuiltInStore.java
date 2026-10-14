/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import java.io.IOException;
import java.io.InputStream;

/**
 * The store shipped in the plugin's jar ({@code store/bloodnight/}), made at build time by {@link StoreGenerator}.
 */
public final class BuiltInStore {
    /**
     * The built-in store's id.
     */
    public static final String ID = "bloodnight";

    /**
     * The pack the setup picks by default.
     */
    public static final String CLASSIC = "classic";

    private static final String PATH = "store/bloodnight/";

    private BuiltInStore() {}

    /**
     * @return the store
     */
    public static Store load() {
        ClassLoader loader = BuiltInStore.class.getClassLoader();
        return StoreFiles.read(name -> {
            InputStream stream = loader.getResourceAsStream(PATH + name);
            if (stream == null) throw new IOException("Missing " + PATH + name);
            return stream;
        });
    }
}
