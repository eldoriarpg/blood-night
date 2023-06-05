package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DivideNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new IntegerNode(30));
        nodeContainer.add(2, new IntegerNode(15));
        var divideNode = new DivideNode();
        divideNode.input()
                .connect(nodeContainer, Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(nodeContainer, Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = divideNode.output(nodeContainer);
        assertEquals(2.0, output.value(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }
}
