package de.eldoria.graph.platform;

import java.util.List;

/**
 * Thrown when a platform's definitions do not fit together.
 */
public final class PlatformException extends RuntimeException {
    private final List<String> problems;

    PlatformException(List<String> problems) {
        super(String.join("\n", problems));
        this.problems = List.copyOf(problems);
    }

    /**
     * @return one message per problem
     */
    public List<String> problems() {
        return problems;
    }
}
