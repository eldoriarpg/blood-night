package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.action.impl.PrintNode;
import de.eldoria.bloodnight.nodes.base.Field;
import de.eldoria.bloodnight.nodes.input.impl.IntegerNode;
import de.eldoria.bloodnight.nodes.trigger.impl.DebugNode;
import org.junit.jupiter.api.Test;

class NodeContainerTest {

    @Test
    public void test() {
        DebugNode debugNode = new DebugNode().chain("first", 1);
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(0, debugNode);
        PrintNode printNode = new PrintNode();
        nodeContainer.add(1, printNode);
        nodeContainer.add(2, new IntegerNode(10));
        printNode.input().set(nodeContainer, "value", new Field(2, "value"));
        debugNode.invoke(nodeContainer);
    }
}
