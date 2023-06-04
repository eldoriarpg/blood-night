package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.input.impl.StringNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsNullNodeTest {

    @ParameterizedTest
    @MethodSource("data")
    void output(String value, boolean result) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new StringNode(value));
        var isNullNode = new IsNullNode();
        isNullNode.input().connect(nodeContainer, Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = isNullNode.output(nodeContainer);
        assertEquals(result, output.value(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> data() {
        return Stream.of(Arguments.of("", false), Arguments.of(null, true));
    }
}
