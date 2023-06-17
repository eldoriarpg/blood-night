package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiplyNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new IntegerNode(4));
        nodeContainer.add(2, new IntegerNode(6));
        var multiplyNode = new MultiplyNode();
        multiplyNode.input()
                .connect(Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = multiplyNode.output();
        assertEquals(24, (double) output.value(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }
}
