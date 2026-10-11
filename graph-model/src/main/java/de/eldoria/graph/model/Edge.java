/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import java.util.Objects;

/**
 * A data edge pointing at the output of another node.
 *
 * @param node id of the node providing the value
 * @param port id of the output port on that node
 */
public record Edge(String node, String port) {
    public Edge {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(port, "port");
    }

    /**
     * Creates an edge.
     *
     * @param node id of the node providing the value
     * @param port id of the output port on that node
     * @return the edge
     */
    public static Edge to(String node, String port) {
        return new Edge(node, port);
    }
}
