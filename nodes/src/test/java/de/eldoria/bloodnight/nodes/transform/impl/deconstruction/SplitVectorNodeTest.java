package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SplitVectorNodeTest {

    @Test
    void output() {
        NodeContainer nodeContainer = new NodeContainer();
        Vector vector = new Vector(1, 2, 3);
        nodeContainer.add(1, new StubValueNode(vector, DataType.VECTOR));
        var splitVectorNode = nodeContainer.add(2, new SplitVectorNode());
        splitVectorNode.input().connect(Fields.VECTOR, new Edge(1, Fields.VALUE));
        var output = splitVectorNode.output();

        assertEquals(vector.getX(), output.value(Fields.X));
        assertEquals(vector.getY(), output.value(Fields.Y));
        assertEquals(vector.getZ(), output.value(Fields.Z));
        assertEquals(DataType.NUMBER, output.getType(Fields.X));
        assertEquals(DataType.NUMBER, output.getType(Fields.Y));
        assertEquals(DataType.NUMBER, output.getType(Fields.Z));
    }
}
