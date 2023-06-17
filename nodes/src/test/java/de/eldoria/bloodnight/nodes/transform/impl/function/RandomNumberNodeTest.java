package de.eldoria.bloodnight.nodes.transform.impl.function;

import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.NumberNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomNumberNodeTest {

    @Test
    void output() {
                NodeContainer container = new NodeContainer();
        container.add(0, new NumberNode(3));
        container.add(1, new NumberNode(33.3));

        var randomIntegerNode = container.add(2,new RandomNumberNode());
        randomIntegerNode.input()
                .connect(Fields.LOWER, new Edge(0, Fields.VALUE))
                .connect(Fields.UPPER, new Edge(1, Fields.VALUE));
        var o = (double) randomIntegerNode.output().value(Fields.RESULT);
        assertTrue(3 < o && o < 33.3);
    }
}
