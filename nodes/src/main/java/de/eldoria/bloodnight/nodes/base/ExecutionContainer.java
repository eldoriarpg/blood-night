package de.eldoria.bloodnight.nodes.base;

import java.util.Map;
import java.util.Set;

public class ExecutionContainer {
    private final Map<String, Set<Integer>> nextNodes;

    public ExecutionContainer(Map<String, Set<Integer>> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public boolean isEmpty() {
        return nextNodes.isEmpty();
    }

    public Set<String> names() {
        return nextNodes.keySet();
    }

    public Set<Integer> next(String out) {
        return nextNodes.get(out);
    }

    public boolean hasName(String name) {
        return nextNodes.containsKey(name);
    }
}
