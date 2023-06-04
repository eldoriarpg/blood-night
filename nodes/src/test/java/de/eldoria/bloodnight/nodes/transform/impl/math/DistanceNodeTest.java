package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        Vector vector1 = new Vector(1, 2, 3);
        Vector vector2 = new Vector(4, 5, 6);
        nodeContainer.add(1, new StubValueNode(vector1, DataType.VECTOR));
        nodeContainer.add(2, new StubValueNode(vector2, DataType.VECTOR));
        var distanceNode = new DistanceNode();
        distanceNode.input().connect(nodeContainer, Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(nodeContainer, Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = distanceNode.output(nodeContainer);

        assertEquals(vector1.distance(vector2), output.value(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }
}
