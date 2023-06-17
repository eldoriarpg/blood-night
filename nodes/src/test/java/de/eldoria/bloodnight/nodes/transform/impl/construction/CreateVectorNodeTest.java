package de.eldoria.bloodnight.nodes.transform.impl.construction;

import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CreateVectorNodeTest {

    @ParameterizedTest
    @MethodSource("outputData")
    void output(int x, int y, int z) {
        NodeContainer container = new NodeContainer();
        CreateVectorNode createVectorNode = new CreateVectorNode();
        container.add(0, createVectorNode);
        container.add(1, new IntegerNode(x));
        container.add(2, new IntegerNode(y));
        container.add(3, new IntegerNode(z));
        createVectorNode.input().connect(Fields.X, new Edge(1, Fields.VALUE));
        createVectorNode.input().connect(Fields.Y, new Edge(2, Fields.VALUE));
        createVectorNode.input().connect(Fields.Z, new Edge(3, Fields.VALUE));
        Assertions.assertEquals(new Vector(x, y, z), createVectorNode.output().value(Fields.RESULT));
    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(0, 0, 0),
                Arguments.of(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE / 2),
                Arguments.of(1000, 100, 10),
                Arguments.of(-1000, 100, 10)
        );
    }
}
