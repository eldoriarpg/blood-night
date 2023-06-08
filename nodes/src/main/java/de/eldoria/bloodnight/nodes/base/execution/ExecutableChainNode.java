package de.eldoria.bloodnight.nodes.base.execution;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.ExecutionContainer;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;
import de.eldoria.bloodnight.util.Checks;

import java.util.Map;
import java.util.Set;

/**
 * A base node which is executable and executes one or more following nodes.
 * @param <T> type of node
 */
public sealed class ExecutableChainNode<T extends ExecutableChainNode<T>> extends ExecutableNode permits ControlFlowNode, TriggerNode {
    @JsonProperty
    private final ExecutionContainer executions = new ExecutionContainer(MetadataReader.readExecutions(this.getClass()));

    public ExecutableChainNode() {
        if (executions.isEmpty()) {
            throw new IllegalStateException("No chain nodes defined in " + getClass().getName());
        }
    }

    public ExecutableChainNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
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
            executions().invokeNext(container, out);
        }
    }

    public ExecutionContainer executions() {
        return executions;
    }

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }
}
