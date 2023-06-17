package de.eldoria.bloodnight.nodes.value.impl;

import de.eldoria.bloodnight.nodes.meta.Fields;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegerNodeTest {

    @Test
    void output() {
        var node = new IntegerNode(007);
        assertEquals(007, (int) node.output().value(Fields.VALUE));
    }
}
