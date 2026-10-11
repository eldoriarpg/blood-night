package de.eldoria.bloodnight.editor;

import de.eldoria.graph.model.GraphJson;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * The editor catalog generated at build time and shipped inside the plugin jar.
 */
public final class BundledCatalog {
    private static final String RESOURCE = "/catalog.json";

    private BundledCatalog() {
    }

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

    /**
     * @return the catalog's version, or {@code unknown} when the jar contains none
     */
    public static String version() {
        return json().map(json -> GraphJson.mapper().readTree(json).path("version").asString("unknown")).orElse("unknown");
    }
}
