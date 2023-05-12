package de.eldoria.bloodnight.nodes.base;

import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.util.Checks;

import java.util.Set;

public class ExecutableChainNode<T extends ExecutableChainNode<T>> extends ExecutableNode {
    private final ExecutionContainer executions = new ExecutionContainer(MetadataReader.readExecutions(this.getClass()));

    public ExecutableChainNode() {
        if (executions.isEmpty()) {
            throw new IllegalStateException("No chain nodes defined in " + getClass().getName());
        }
    }

    public T chain(String name, int next) {
        Set<Integer> nextNodes = executions.next(name);
        Checks.notNull(nextNodes, "The field %s is not available as execution output. Available fields: %s".formatted(name, String.join(", ", executions.names())));
        nextNodes.add(next);
        return self();
    }

    @Override
    public void invoke(NodeContainer container) {
        for (var out : executions.names()) {
            for (Integer nextNode : executions.next(out)) {
                if (container.get(nextNode) instanceof ExecutableNode exec) {
                    exec.invoke(container);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }
}
