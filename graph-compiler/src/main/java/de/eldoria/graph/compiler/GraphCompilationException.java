package de.eldoria.graph.compiler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Thrown when a graph cannot be turned into a class. Carries every problem found, each located at a node where
 * possible.
 */
public final class GraphCompilationException extends Exception {
    private final List<GraphProblem> problems;
    private final String source;

    GraphCompilationException(List<GraphProblem> problems, String source) {
        super(problems.stream().map(GraphProblem::toString).collect(Collectors.joining("\n", "Graph does not compile:\n", "")));
        this.problems = List.copyOf(problems);
        this.source = source;
    }

    /**
     * @return errors and warnings
     */
    public List<GraphProblem> problems() {
        return problems;
    }

    /**
     * @return the generated source when the failure happened while compiling it, for debugging
     */
    public Optional<String> source() {
        return Optional.ofNullable(source);
    }
}
