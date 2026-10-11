package de.eldoria.bloodnight.editor;

import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.CatalogGenerator;
import de.eldoria.graph.platform.GraphPlatform;

/**
 * The editor catalog of the server as it runs: nodes reflected from the server's API and registry values from its
 * live registries, datapack entries included. Descriptions come from the catalog bundled at build time, which had the
 * API's javadoc; nodes the bundled catalog does not know keep empty descriptions.
 */
public final class RuntimeCatalog {
    private RuntimeCatalog() {
    }

    /**
     * @param platform the platform the server compiles with
     * @return its catalog
     */
    public static Catalog create(GraphPlatform platform) {
        Catalog live = CatalogGenerator.generate(platform);
        return BundledCatalog.json()
                .map(CatalogGenerator::read)
                .map(bundled -> CatalogGenerator.withDescriptions(live, bundled))
                .orElse(live);
    }
}
