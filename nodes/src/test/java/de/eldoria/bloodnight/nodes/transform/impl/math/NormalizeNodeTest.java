package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizeNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        Vector vector = new Vector(1, 2, 3);
        nodeContainer.add(1, new StubValueNode(vector, DataType.VECTOR));
        var distanceNode = new NormalizeNode();
        distanceNode.input().connect(Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = distanceNode.output();

        assertEquals(vector.normalize(), output.value(Fields.RESULT));
        assertEquals(DataType.VECTOR, output.getType(Fields.RESULT));
    }
}
