/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.CatalogGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Stores the catalogs servers run, by version. A version names one exact catalog, so read catalogs are kept in
 * memory.
 */
final class CatalogRepository {
    private final Map<String, Catalog> cache = new ConcurrentHashMap<>();

    boolean exists(String version) {
        if (cache.containsKey(version)) return true;
        return query("SELECT 1 FROM editor_catalog WHERE version = :version;")
                .single(call().bind("version", version))
                .map(row -> true)
                .first()
                .isPresent();
    }

    void store(String version, String catalog) {
        query("""
                INSERT INTO editor_catalog(version, catalog) VALUES (:version, CAST(:catalog AS JSONB))
                ON CONFLICT (version) DO NOTHING;""")
                .single(call().bind("version", version).bind("catalog", catalog))
                .insert();
    }

    Optional<Catalog> find(String version) {
        Catalog cached = cache.get(version);
        if (cached != null) return Optional.of(cached);
        Optional<Catalog> catalog = query(
                        "SELECT catalog::TEXT AS catalog FROM editor_catalog WHERE version = :version;")
                .single(call().bind("version", version))
                .map(row -> CatalogGenerator.read(row.getString("catalog")))
                .first();
        catalog.ifPresent(found -> cache.put(version, found));
        return catalog;
    }
}
