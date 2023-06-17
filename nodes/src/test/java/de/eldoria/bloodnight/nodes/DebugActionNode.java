package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.container.NodeContainer;

public class DebugActionNode extends ActionNode {

    private final String name;
    private int timesExecuted = 0;

    public DebugActionNode(String name) {
        this.name = name;
    }

    @Override
    public void invoke(NodeContainer container) {
        timesExecuted++;
    }

    public boolean executed() {
        return timesExecuted != 0;
    }

    public int timesExecuted() {
        return timesExecuted;
    }

    public String name() {
        return name;
    }
}
