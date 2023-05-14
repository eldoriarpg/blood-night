package de.eldoria.bloodnight.nodes.base.execution;

import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.base.Node;

public sealed abstract class ExecutableNode extends Node permits ActionNode, ExecutableChainNode {
    public abstract void invoke(NodeContainer container);
}
