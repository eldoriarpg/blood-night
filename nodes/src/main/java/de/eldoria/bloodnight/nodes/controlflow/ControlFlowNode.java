package de.eldoria.bloodnight.nodes.controlflow;

import de.eldoria.bloodnight.nodes.base.execution.ExecutableChainNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.controlflow.impl.BatchNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.BranchNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.ForEachNode;

import java.util.Map;

/**
 * A node representing a node used for flow control. E.g.: {@link BranchNode}, {@link BatchNode} or {@link ForEachNode}.
 *
 * @param <T> type of node
 */
public non-sealed class ControlFlowNode<T extends ControlFlowNode<T>> extends ExecutableChainNode<T> {
    public ControlFlowNode() {
    }

    public ControlFlowNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
    }
}
