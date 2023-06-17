package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.NumberNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(30.2));
        var roundNode = new RoundNode();
        roundNode.input().connect(Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = roundNode.output();
        assertEquals(30, output.value(Fields.RESULT));
        assertEquals(DataType.INTEGER, output.getType(Fields.RESULT));
    }
}
