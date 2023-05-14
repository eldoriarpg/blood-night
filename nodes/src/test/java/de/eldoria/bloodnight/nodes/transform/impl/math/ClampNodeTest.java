package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Field;
import de.eldoria.bloodnight.nodes.input.impl.NumberNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ClampNodeTest {

    @ParameterizedTest
    @MethodSource("outputData")
    void output(double value, double lower, double upper, double result) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(value));
        nodeContainer.add(2, new NumberNode(lower));
        nodeContainer.add(3, new NumberNode(upper));
        var clampNode = new ClampNode();
        clampNode.input()
                .set(nodeContainer, Fields.VALUE, new Field(1, Fields.VALUE))
                .set(nodeContainer, Fields.LOWER, new Field(2, Fields.VALUE))
                .set(nodeContainer, Fields.UPPER, new Field(3, Fields.VALUE));
        var output = clampNode.output(nodeContainer);
        assertEquals(result, output.get(Fields.RESULT));
        assertEquals(DataType.NUMBER, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(35.5, 30.0, 32.0, 32.0),
                Arguments.of(28.0, 30.0, 32.0, 30.0),
                Arguments.of(31.0, 30.0, 32.0, 31.0)
        );
    }
}
