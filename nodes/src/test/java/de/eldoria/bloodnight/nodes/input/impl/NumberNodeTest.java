package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberNodeTest {

    @Test
    void output() {
        var node = new NumberNode(4.9);
        assertEquals(4.9, (double) node.output(new NodeContainer()).value(Fields.VALUE));
    }
}
