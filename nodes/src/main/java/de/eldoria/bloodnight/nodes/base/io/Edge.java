package de.eldoria.bloodnight.nodes.base.io;

/**
 * Record representing the edge of a node
 * @param node ID of the linked node
 * @param name Name of the linked field
 */
public record Edge(
        int node,
        String name
) {
}
