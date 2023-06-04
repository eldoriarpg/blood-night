package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.input.impl.BooleanNode;
import de.eldoria.bloodnight.nodes.input.impl.IntegerNode;
import de.eldoria.bloodnight.nodes.input.impl.NumberNode;
import de.eldoria.bloodnight.nodes.input.impl.StringNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EqualNodeTest {

    @ParameterizedTest
    @MethodSource("outputData")
    void output(Node firstNode, Node secondNode, boolean result) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, firstNode);
        nodeContainer.add(2, secondNode);
        var equalNode = new EqualNode();
        equalNode.input()
                .connect(nodeContainer, Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(nodeContainer, Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = equalNode.output(nodeContainer);
        assertEquals(result, output.value(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(new IntegerNode(42), new IntegerNode(42), true),
                Arguments.of(new IntegerNode(42), new IntegerNode(69), false),
                Arguments.of(new StringNode("uwu"), new StringNode("uwu"), true),
                Arguments.of(new StringNode("uwu"), new StringNode("owo"), false),
                Arguments.of(new StringNode("42"), new IntegerNode(42), false),
                Arguments.of(new NumberNode(42.0), new IntegerNode(42), false),
                Arguments.of(new NumberNode(42.0), new NumberNode(42.0), true),
                Arguments.of(new BooleanNode(true), new BooleanNode(true), true),
                Arguments.of(new BooleanNode(false), new BooleanNode(true), false),
                Arguments.of(new BooleanNode(true), new BooleanNode(false), false),
                Arguments.of(new BooleanNode(false), new BooleanNode(false), true),
                Arguments.of(new BooleanNode(false), new BooleanNode(false), true)
        );
    }
}
