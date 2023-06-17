package de.eldoria.bloodnight.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eldoria.bloodnight.nodes.action.impl.PrintNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.trigger.impl.DebugNode;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.junit.jupiter.api.Test;

class NodeContainerTest {

    @Test
    public void test() throws JsonProcessingException {
        DebugNode debugNode = new DebugNode().chain(Fields.NEXT, 1);
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(0, debugNode);
        PrintNode printNode = new PrintNode();
        nodeContainer.add(1, printNode);
        nodeContainer.add(2, new IntegerNode(10));
        printNode.input().connect(Fields.VALUE, new Edge(2, Fields.VALUE));
        debugNode.invoke(nodeContainer);

        String asString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(nodeContainer);
        System.out.println(asString);
        new ObjectMapper().readValue(asString, NodeContainer.class);
    }
}
