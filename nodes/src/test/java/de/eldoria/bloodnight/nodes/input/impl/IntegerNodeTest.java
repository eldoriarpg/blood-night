package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegerNodeTest {

    @Test
    void output() {
        var node = new IntegerNode(007);
        assertEquals(007, (int) node.output(new NodeContainer()).get(Fields.VALUE));
    }
}
