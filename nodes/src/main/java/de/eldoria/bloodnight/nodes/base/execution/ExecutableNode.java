package de.eldoria.bloodnight.nodes.base.execution;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;

import java.util.Map;

/**
 * A node which is executable.
 */
@JsonPropertyOrder(alphabetic = true)
public sealed abstract class ExecutableNode extends Node permits ActionNode, ExecutableChainNode {
    public ExecutableNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
    }

    public ExecutableNode() {
    }

    public abstract void invoke(NodeContainer container);
}
