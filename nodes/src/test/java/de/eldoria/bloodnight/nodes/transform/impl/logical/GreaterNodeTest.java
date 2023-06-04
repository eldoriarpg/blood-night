package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.input.impl.IntegerNode;
import de.eldoria.bloodnight.nodes.input.impl.NumberNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GreaterNodeTest {

    @ParameterizedTest
    @MethodSource("outputData")
    void output(Node first, Node second, boolean result) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, first);
        nodeContainer.add(2, second);
        var greaterNode = new GreaterNode();
        greaterNode.input()
                .connect(nodeContainer, Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(nodeContainer, Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = greaterNode.output(nodeContainer);
        assertEquals(result, output.value(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(new NumberNode(133.7), new NumberNode(120.0), true),
                Arguments.of(new NumberNode(120.0), new NumberNode(133.7), false),
                Arguments.of(new IntegerNode(120), new NumberNode(133.7), false),
                Arguments.of(new IntegerNode(133), new NumberNode(120.0), true),
                Arguments.of(new IntegerNode(133), new NumberNode(133), false)
        );
    }
}
