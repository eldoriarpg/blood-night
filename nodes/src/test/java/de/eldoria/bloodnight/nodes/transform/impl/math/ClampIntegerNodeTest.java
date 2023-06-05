package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.NumberNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ClampIntegerNodeTest {
    @ParameterizedTest
    @MethodSource("outputData")
    void output(int value, int lower, int upper, int result) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(value));
        nodeContainer.add(2, new NumberNode(lower));
        nodeContainer.add(3, new NumberNode(upper));
        var clampNode = new ClampIntegerNode();
        clampNode.input()
                .connect(nodeContainer, Fields.VALUE, new Edge(1, Fields.VALUE))
                .connect(nodeContainer, Fields.LOWER, new Edge(2, Fields.VALUE))
                .connect(nodeContainer, Fields.UPPER, new Edge(3, Fields.VALUE));
        var output = clampNode.output(nodeContainer);
        assertEquals(result, (int) output.value(Fields.RESULT));
        assertEquals(DataType.INTEGER, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(35, 30, 32, 32),
                Arguments.of(28, 30, 32, 30),
                Arguments.of(31, 30, 32, 31)
        );
    }
}
