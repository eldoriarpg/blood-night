package de.eldoria.bloodnight.nodes.base.io;

import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.execution.ExecutableNode;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class ExecutionContainer {
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

    public Collection<ExecutableNode> nextNodes(NodeContainer container, String name) {
        return next(name)
                .stream()
                .map(container::get)
                .map(ExecutableNode.class::cast)
                .toList();

    }

    public void invokeNext(NodeContainer container, String name) {
        for (var node : nextNodes(container, name)) {
            node.invoke(container);
        }
    }

    public boolean hasName(String name) {
        return nextNodes.containsKey(name);
    }
}
