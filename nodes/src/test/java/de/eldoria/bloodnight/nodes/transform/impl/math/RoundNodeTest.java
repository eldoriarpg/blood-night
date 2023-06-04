package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.input.impl.NumberNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(30.2));
        var roundNode = new RoundNode();
        roundNode.input().connect(nodeContainer, Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = roundNode.output(nodeContainer);
        assertEquals(30, output.value(Fields.RESULT));
        assertEquals(DataType.INTEGER, output.getType(Fields.RESULT));
    }
}
