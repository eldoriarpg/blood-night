package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.value.impl.BooleanNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrNodeTest {

    @ParameterizedTest
    @MethodSource("outputData")
    void output(boolean first, boolean second, boolean result) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new BooleanNode(first));
        nodeContainer.add(2, new BooleanNode(second));
        var orNode = nodeContainer.add(3, new OrNode());
        orNode.input()
                .connect(Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = orNode.output();
        assertEquals(result, output.value(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(true, false, true),
                Arguments.of(false, true, true),
                Arguments.of(false, false, false)
        );
    }
}
