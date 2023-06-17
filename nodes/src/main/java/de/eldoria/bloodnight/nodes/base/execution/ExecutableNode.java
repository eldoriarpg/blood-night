package de.eldoria.bloodnight.nodes.base.execution;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;

import java.util.Map;

/**
 * A node which is executable.
 * <p>
 *
 * An {@link ExecutableNode} is placed at the end of an execution chain, which consists of {@link ExecutableChainNode}s.
 *
 * See {@link ActionNode} and {@link ExecutableChainNode} for implementation details.
 */
@JsonPropertyOrder(alphabetic = true)
public sealed abstract class ExecutableNode extends Node permits ActionNode, ExecutableChainNode {
    public ExecutableNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
    }

    public ExecutableNode() {
    }

    /**
     * Executes this node.
     *
     * @param container container to which this node belongs
     */
    public abstract void invoke(NodeContainer container);
}
