package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceSqrtNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        Vector vector1 = new Vector(1, 2, 3);
        Vector vector2 = new Vector(4, 5, 6);
        nodeContainer.add(1, new StubValueNode(vector1, DataType.VECTOR));
        nodeContainer.add(2, new StubValueNode(vector2, DataType.VECTOR));
        var distanceSqrtNode = nodeContainer.add(3, new DistanceSqrtNode());
        distanceSqrtNode.input().connect(Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = distanceSqrtNode.output();

        assertEquals(vector1.distanceSquared(vector2), output.value(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }
}
