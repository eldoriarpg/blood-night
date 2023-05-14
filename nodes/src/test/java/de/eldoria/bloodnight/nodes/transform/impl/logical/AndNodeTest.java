package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Field;
import de.eldoria.bloodnight.nodes.input.impl.BooleanNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AndNodeTest {

    @ParameterizedTest
    @MethodSource("outputData")
    void output(boolean first, boolean second, boolean result) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new BooleanNode(first));
        nodeContainer.add(2, new BooleanNode(second));
        var andNode = new AndNode();
        andNode.input()
                    .set(nodeContainer, Fields.FIRST, new Field(1, Fields.VALUE))
                .set(nodeContainer, Fields.SECOND, new Field(2, Fields.VALUE));
        var output = andNode.output(nodeContainer);
        assertEquals(result, output.get(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(true, false, false),
                Arguments.of(false, true, false),
                Arguments.of(false, false, false)
        );
    }
}
