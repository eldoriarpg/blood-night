/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload;

import de.eldoria.graph.payload.node.NodeType;

import java.util.List;
import java.util.Map;

/**
 * Everything the editor needs to know about a platform to build graphs for it, independent of any user data.
 * <p>
 * This is the contract between the framework and the editor: the editor renders palettes, ports, typed-in values and
 * connection rules from it without knowing any node.
 *
 * @param format     version of this structure
 * @param version    hash of the content, so user data can name the catalog it was made with
 * @param nodes      every node, ordered by id
 * @param types      every concrete type used by a port, ordered by id
 * @param selectable types users can pick themselves, e.g. for macro inputs, by id
 * @param bindings   graph-wide type variables by name with the id of the type they have when nothing binds them,
 *                   e.g. {@code SELF} to the living entity type; ports typed by such a variable take the type a graph
 *                   is compiled for, which the editor learns elsewhere
 */
public record Catalog(
        int format,
        String version,
        List<NodeType> nodes,
        List<TypeInfo> types,
        Map<String, String> selectable,
        Map<String, String> bindings) {
    /**
     * A catalog without graph-wide type variables.
     */
    public Catalog(
            int format, String version, List<NodeType> nodes, List<TypeInfo> types, Map<String, String> selectable) {
        this(format, version, nodes, types, selectable, Map.of());
    }

    public Catalog {
        bindings = bindings == null ? Map.of() : Map.copyOf(bindings);
    }

    /**
     * Current value of {@link #format()}.
     */
    public static final int FORMAT = 1;
}
