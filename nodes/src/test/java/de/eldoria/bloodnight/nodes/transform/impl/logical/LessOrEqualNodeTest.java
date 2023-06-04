package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.input.impl.NumberNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class LessOrEqualNodeTest {

    @ParameterizedTest
    @CsvSource({
            "1.0,1.1,true",
            "1.1,1.0,false",
            "1.0,1.0,true"
    })
    void output(double first, double second, boolean expectedResult) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new NumberNode(first));
        nodeContainer.add(2, new NumberNode(second));
        var lessOrEqualNode = new LessOrEqualNode();
        lessOrEqualNode.input().connect(nodeContainer, Fields.FIRST, new Edge(1, Fields.VALUE))
                .connect(nodeContainer, Fields.SECOND, new Edge(2, Fields.VALUE));
        var output = lessOrEqualNode.output(nodeContainer);
        assertEquals(expectedResult, output.value(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }
}
