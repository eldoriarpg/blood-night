package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.BooleanNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class NotNodeTest {
    @ParameterizedTest
    @CsvSource({
            "true,false",
            "false,true"
    })
    void output(boolean first, boolean expectedResult) {
        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new BooleanNode(first));
        var notNode = new NotNode();
        notNode.input().connect(nodeContainer, Fields.FIRST, new Edge(1, Fields.VALUE));
        var output = notNode.output(nodeContainer);
        assertEquals(expectedResult, output.value(Fields.RESULT));
        assertEquals(DataType.BOOLEAN, output.getType(Fields.RESULT));
    }
}
