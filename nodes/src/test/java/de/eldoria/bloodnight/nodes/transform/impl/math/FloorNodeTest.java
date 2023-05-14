package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Field;
import de.eldoria.bloodnight.nodes.input.impl.NumberNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloorNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(30.2));
        var floorNode = new FloorNode();
        floorNode.input().set(nodeContainer, Fields.VALUE, new Field(1, Fields.VALUE));
        var output = floorNode.output(nodeContainer);
        assertEquals(30, (int) output.get(Fields.RESULT));
        assertEquals(DataType.INTEGER, output.getType(Fields.RESULT));
    }
}
