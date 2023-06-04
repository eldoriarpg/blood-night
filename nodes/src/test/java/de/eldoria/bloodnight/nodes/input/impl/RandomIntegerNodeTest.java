package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomIntegerNodeTest {

    @Test
    void output() {
        NodeContainer container = new NodeContainer();
        container.add(0, new IntegerNode(3));
        container.add(1, new IntegerNode(787));

        RandomIntegerNode randomIntegerNode = new RandomIntegerNode();
        randomIntegerNode.input()
                .connect(container, Fields.LOWER, new Edge(0, Fields.VALUE))
                .connect(container, Fields.UPPER, new Edge(1, Fields.VALUE));
        container.add(2, randomIntegerNode);
        int o = (int) randomIntegerNode.output(container).value(Fields.RESULT);
        assertTrue(3 < o && o < 787);
    }
}
