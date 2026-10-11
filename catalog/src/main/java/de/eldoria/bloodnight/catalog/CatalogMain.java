/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.catalog;

import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.CatalogGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Writes the editor catalog of the blood-night platform at build time, without a server.
 * <p>
 * Arguments: the output file, then the sources jars to take descriptions from.
 */
public final class CatalogMain {
    private CatalogMain() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 1) throw new IllegalArgumentException("Usage: <output> [sources jars...]");
        Path output = Path.of(args[0]);
        List<Path> sources = Arrays.stream(args).skip(1).map(Path::of).toList();
        try (JavadocDocumentation documentation = new JavadocDocumentation(sources)) {
            Catalog catalog = CatalogGenerator.generate(BloodNightPlatform.create(List.of(), documentation));
            Path directory = output.toAbsolutePath().getParent();
            if (directory != null) Files.createDirectories(directory);
            Files.writeString(output, CatalogGenerator.write(catalog));
            System.out.printf(
                    "Catalog %s: %d nodes, %d types -> %s%n",
                    catalog.version(), catalog.nodes().size(), catalog.types().size(), output);
        }
    }
}
