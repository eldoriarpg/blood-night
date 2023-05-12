package de.eldoria.bloodnight.nodes.base;

import de.eldoria.bloodnight.nodes.NodeContainer;

public abstract class ExecutableNode extends Node {
    public abstract void invoke(NodeContainer container);
}
