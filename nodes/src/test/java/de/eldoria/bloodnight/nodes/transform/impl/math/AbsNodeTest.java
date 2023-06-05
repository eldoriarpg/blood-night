package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.NumberNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbsNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(-36.0));
        var absNode = new AbsNode();
        absNode.input().connect(nodeContainer, Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = absNode.output(nodeContainer);
        assertEquals(36.0, output.value(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }
}
