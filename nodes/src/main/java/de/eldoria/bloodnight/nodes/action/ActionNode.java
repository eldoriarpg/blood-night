package de.eldoria.bloodnight.nodes.action;

import de.eldoria.bloodnight.nodes.base.execution.ExecutableNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;

import java.util.Map;

/**
 * Base node for nodes which executes actions. Usually at the end of an execution chain.
 */
public non-sealed abstract class ActionNode extends ExecutableNode {

    public ActionNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
    }

    public ActionNode() {
    }
}
