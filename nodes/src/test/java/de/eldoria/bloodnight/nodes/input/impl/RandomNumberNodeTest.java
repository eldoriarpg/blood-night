package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomNumberNodeTest {

    @Test
    void output() {
                NodeContainer container = new NodeContainer();
        container.add(0, new NumberNode(3));
        container.add(1, new NumberNode(33.3));

        var randomIntegerNode = new RandomNumberNode();
        randomIntegerNode.input()
                .set(container, Fields.LOWER, new Field(0, Fields.VALUE))
                .set(container, Fields.UPPER, new Field(1, Fields.VALUE));
        container.add(2, randomIntegerNode);
        var o = (double) randomIntegerNode.output(container).get(Fields.RESULT);
        assertTrue(3 < o && o < 33.3);
    }
}
