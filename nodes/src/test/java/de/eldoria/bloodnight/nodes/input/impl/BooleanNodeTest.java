package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanNodeTest {

    @Test
    void output() {
        BooleanNode node = new BooleanNode(true);
        assertTrue((boolean) node.output(new NodeContainer()).get(Fields.VALUE));
    }
}
