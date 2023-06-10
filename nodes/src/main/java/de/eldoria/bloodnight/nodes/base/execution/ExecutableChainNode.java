package de.eldoria.bloodnight.nodes.base.execution;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Execution;
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
 * <p>
 * Nodes of this type require at least one {@link Execution} annotation.
 * <p>
 * An execution node can have an "unlimited" number of execution fields.
 *
 * See {@link ControlFlowNode} and {@link TriggerNode} for implementation details.
 *
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

    /**
     * Chain another node, which will be triggered on execution of this node.
     * A node can be chained with multiple nodes.
     * There is no guarantee about an order of execution.
     * In general node executions should always work independent of each other.
     *
     * @param name name of the output name of the {@link Execution#value()}
     * @param next id of the next node in the {@link NodeContainer}
     * @return this instance for chaining
     */
    public T chain(String name, int next) {
        Set<Integer> nextNodes = executions.next(name);
        Checks.notNull(nextNodes, "The field %s is not available as execution output. Available fields: %s".formatted(name, String.join(", ", executions.names())));
        nextNodes.add(next);
        return self();
    }

    /**
     * Invokes this node and calls the next nodes.
     * <p>
     * The next nodes are defined via {@link #chain(String, int)} where a node can be linked to an output field defined by the {@link Execution} annotation
     *
     * @param container container to which this node belongs
     */
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
