package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.action.impl.PrintNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import de.eldoria.bloodnight.nodes.trigger.impl.DebugNode;
import org.junit.jupiter.api.Test;

class NodeContainerTest {

    @Test
    public void test() {
        DebugNode debugNode = new DebugNode().chain(Fields.NEXT, 1);
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(0, debugNode);
        PrintNode printNode = new PrintNode();
        nodeContainer.add(1, printNode);
        nodeContainer.add(2, new IntegerNode(10));
        printNode.input().connect(nodeContainer, Fields.VALUE, new Edge(2, Fields.VALUE));
        debugNode.invoke(nodeContainer);
    }
}
