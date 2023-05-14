package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Field;
import de.eldoria.bloodnight.nodes.input.impl.IntegerNode;
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
                .set(nodeContainer, Fields.FIRST, new Field(1, Fields.VALUE))
                .set(nodeContainer, Fields.SECOND, new Field(2, Fields.VALUE));
        var output = multiplyNode.output(nodeContainer);
        assertEquals(24, (double) output.get(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }
}
