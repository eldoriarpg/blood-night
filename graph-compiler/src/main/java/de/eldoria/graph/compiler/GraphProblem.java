package de.eldoria.graph.compiler;

import java.util.Objects;

/**
 * Something wrong with a graph, located as precisely as possible so the editor can highlight it.
 *
 * @param severity whether compilation fails because of it
 * @param node     id of the offending node instance, or {@code null} for the graph as a whole
 * @param port     id of the offending port or config field, or {@code null}
 * @param message  what is wrong
 */
public record GraphProblem(Severity severity, String node, String port, String message) {
    public GraphProblem {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(message, "message");
    }

    static GraphProblem error(String node, String port, String message) {
        return new GraphProblem(Severity.ERROR, node, port, message);
    }

    static GraphProblem warning(String node, String port, String message) {
        return new GraphProblem(Severity.WARNING, node, port, message);
    }

    @Override
    public String toString() {
        String location = node == null ? "graph" : port == null ? node : node + "." + port;
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
