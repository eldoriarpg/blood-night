package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.input.impl.IntegerNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtractNodeTest {
    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new IntegerNode(42));
        nodeContainer.add(2, new IntegerNode(12));
        var subtractNode = new SubtractNode();
        subtractNode.input().connect(nodeContainer, Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(nodeContainer, Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = subtractNode.output(nodeContainer);
        assertEquals(30.0, (double) output.value(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }
}
