package de.eldoria.bloodnight.nodes.value.impl;

import de.eldoria.bloodnight.nodes.meta.Fields;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanNodeTest {

    @Test
    void output() {
        BooleanNode node = new BooleanNode(true);
        assertTrue((boolean) node.output().value(Fields.VALUE));
    }
}
