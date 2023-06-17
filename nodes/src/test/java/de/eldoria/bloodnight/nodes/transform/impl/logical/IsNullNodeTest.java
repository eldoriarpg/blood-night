package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.StringNode;
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
        var isNullNode = nodeContainer.add(2, new IsNullNode());
        isNullNode.input().connect(Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = isNullNode.output();
        assertEquals(result, output.value(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }

    private static Stream<Arguments> data() {
        return Stream.of(Arguments.of("", false), Arguments.of(null, true));
    }
}
