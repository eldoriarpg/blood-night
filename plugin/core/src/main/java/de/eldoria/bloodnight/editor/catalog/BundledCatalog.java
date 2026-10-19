/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * The editor catalog generated at build time and shipped inside the plugin jar. It describes the API the jar was
 * built against and carries the javadoc texts; the server's own catalog is {@link RuntimeCatalog}.
 */
public final class BundledCatalog {
    private static final String RESOURCE = "/catalog.json";

    private BundledCatalog() {}

    /**
     * @return the catalog JSON, if the jar contains one
     */
    public static Optional<String> json() {
        try (InputStream in = BundledCatalog.class.getResourceAsStream(RESOURCE)) {
            return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
