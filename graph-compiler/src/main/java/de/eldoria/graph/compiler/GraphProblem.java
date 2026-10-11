/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import java.util.List;
import java.util.Objects;

/**
 * Something wrong with a graph, located as precisely as possible so the editor can highlight it.
 *
 * @param severity whether compilation fails because of it
 * @param node     id of the offending node instance in the graph as stored, or {@code null} for the graph as a whole;
 *                 for problems inside a macro, the node using the macro
 * @param port     id of the offending port or config field, or {@code null}
 * @param message  what is wrong
 * @param trace    path into macros when the problem lies inside one, outermost first; the last frame names the
 *                 offending node inside its macro, and {@link #port()} belongs to that node
 */
public record GraphProblem(Severity severity, String node, String port, String message, List<MacroFrame> trace) {
    public GraphProblem {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(message, "message");
        trace = trace == null ? List.of() : List.copyOf(trace);
    }

    static GraphProblem error(String node, String port, String message) {
        return new GraphProblem(Severity.ERROR, node, port, message, List.of());
    }

    static GraphProblem warning(String node, String port, String message) {
        return new GraphProblem(Severity.WARNING, node, port, message, List.of());
    }

    /**
     * @param node  node in the graph as stored
     * @param trace path into macros
     * @return this problem relocated
     */
    GraphProblem at(String node, List<MacroFrame> trace) {
        return new GraphProblem(severity, node, port, message, trace);
    }

    @Override
    public String toString() {
        String location = node == null ? "graph" : node;
        for (MacroFrame frame : trace) location += " > %s:%s".formatted(frame.macro(), frame.node());
        if (port != null) location += "." + port;
        return "%s %s: %s".formatted(severity, location, message);
    }

    /**
     * How serious a problem is.
     */
    public enum Severity {
        /**
         * The graph cannot be compiled.
         */
        ERROR,
        /**
         * The graph compiles, but probably not as intended.
         */
        WARNING
    }
}
