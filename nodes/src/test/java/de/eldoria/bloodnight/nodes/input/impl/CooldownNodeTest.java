package de.eldoria.bloodnight.nodes.input.impl;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Field;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CooldownNodeTest {

    @BeforeAll
    static void beforeAll() {
        ServerMock mock = MockBukkit.mock();
    }


    @Test
    void output() {
        NodeContainer container = new NodeContainer();
        container.add(0, new IntegerNode(187));
        CooldownNode cooldownNode = container.add(1, new CooldownNode());
        cooldownNode.input().set(container, Fields.VALUE, new Field(0, Fields.VALUE));
        Assertions.assertTrue((boolean)cooldownNode.output(container).get(Fields.RESULT));
        Assertions.assertFalse((boolean)cooldownNode.output(container).get(Fields.RESULT));
    }

    @AfterAll
    static void afterAll() {
        MockBukkit.unmock();
    }
}
