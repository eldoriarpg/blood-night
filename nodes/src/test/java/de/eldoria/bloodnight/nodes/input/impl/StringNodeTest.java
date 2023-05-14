package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringNodeTest {

    @Test
    void output() {
        var node = new StringNode("Chojo stinkt");
        assertEquals("Chojo stinkt", node.output(new NodeContainer()).get(Fields.VALUE));
    }
}
