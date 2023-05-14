package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Field;
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
                .set(container, Fields.LOWER, new Field(0, Fields.VALUE))
                .set(container, Fields.UPPER, new Field(1, Fields.VALUE));
        container.add(2, randomIntegerNode);
        int o = (int) randomIntegerNode.output(container).get(Fields.RESULT);
        assertTrue(3 < o && o < 787);
    }
}
