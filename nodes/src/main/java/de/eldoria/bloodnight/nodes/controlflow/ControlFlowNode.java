package de.eldoria.bloodnight.nodes.controlflow;

import de.eldoria.bloodnight.nodes.base.execution.ExecutableChainNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.BatchNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.ForEachNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.BranchNode;

/**
 * A node representing a node used for flow control. E.g.: {@link BranchNode}, {@link BatchNode} or {@link ForEachNode}.
 *
 * @param <T> type of node
 */
public non-sealed class ControlFlowNode<T extends ControlFlowNode<T>> extends ExecutableChainNode<T> {
}
