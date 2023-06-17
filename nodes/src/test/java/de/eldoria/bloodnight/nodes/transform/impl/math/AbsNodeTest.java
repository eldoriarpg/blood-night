package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.NumberNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbsNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(-36.0));
        var absNode = nodeContainer.add(2,new AbsNode());
        absNode.input().connect(Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = absNode.output();
        assertEquals(36.0, output.value(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }

    @Test
    void copy() {
        AbsNode absNode = new AbsNode();
        Node copy = absNode.copy();
        Assertions.assertTrue(copy instanceof AbsNode);
        Assertions.assertNotSame(copy, absNode);
    }
}
