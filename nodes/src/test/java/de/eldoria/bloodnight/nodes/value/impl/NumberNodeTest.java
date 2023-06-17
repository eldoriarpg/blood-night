package de.eldoria.bloodnight.nodes.value.impl;

import de.eldoria.bloodnight.nodes.meta.Fields;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberNodeTest {

    @Test
    void output() {
        var node = new NumberNode(4.9);
        assertEquals(4.9, (double) node.output().value(Fields.VALUE));
    }
}
