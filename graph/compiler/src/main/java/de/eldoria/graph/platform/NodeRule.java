/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.platform;

import de.eldoria.graph.definition.port.PortType;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * A platform's own check of one node, for what port types cannot express: e.g. that the data given to a particle
 * fits the particle typed into the same node. Runs when a graph is validated, after its types are resolved.
 */
@FunctionalInterface
public interface NodeRule {
    /**
     * @param node   the node being checked
     * @param report receives the problems found
     */
    void check(Node node, Report report);

    /**
     * What a rule can see of a node.
     */
    interface Node {
        /**
         * @return the node's id in the graph
         */
        String id();

        /**
         * @param port input port id
         * @return the value typed into the input, if it is typed in rather than connected
         */
        Optional<JsonNode> literal(String port);

        /**
         * @param port input port id
         * @return the type of the output connected to the input, if one is
         */
        Optional<PortType> connected(String port);
    }

    /**
     * Collects the problems a rule finds. Errors stop the graph from compiling, warnings are only shown.
     */
    interface Report {
        /**
         * @param port    the input the problem is about
         * @param message what is wrong and how to fix it
         */
        void error(String port, String message);

        /**
         * @param port    the input the problem is about, {@code null} for the whole node
         * @param message what is not as it should be
         */
        void warning(@Nullable String port, String message);
    }
}
