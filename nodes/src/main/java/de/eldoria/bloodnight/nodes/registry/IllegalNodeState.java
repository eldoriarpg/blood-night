package de.eldoria.bloodnight.nodes.registry;

import de.eldoria.bloodnight.nodes.base.Node;

public class IllegalNodeState extends RuntimeException {
    public IllegalNodeState(Class<? extends Node> nodeClass, String message) {
        super("Error for node %s: ".formatted(nodeClass.getName()) + message);
    }
}
