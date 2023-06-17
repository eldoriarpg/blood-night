package de.eldoria.bloodnight.nodes.transform.impl.function;

import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomIntegerNodeTest {

    @Test
    void output() {
        NodeContainer container = new NodeContainer();
        container.add(0, new IntegerNode(3));
        container.add(1, new IntegerNode(787));

        RandomIntegerNode randomIntegerNode = container.add(2, new RandomIntegerNode());
        randomIntegerNode.input()
                .connect(Fields.LOWER, new Edge(0, Fields.VALUE))
                .connect(Fields.UPPER, new Edge(1, Fields.VALUE));
        int o = (int) randomIntegerNode.output().value(Fields.RESULT);
        assertTrue(3 < o && o < 787);
    }
}
