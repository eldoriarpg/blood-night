package de.eldoria.bloodnight.nodes.transform.impl.function;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
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
        cooldownNode.input().connect(Fields.VALUE, new Edge(0, Fields.VALUE));
        Assertions.assertTrue((boolean)cooldownNode.output().value(Fields.RESULT));
        Assertions.assertFalse((boolean)cooldownNode.output().value(Fields.RESULT));
    }

    @AfterAll
    static void afterAll() {
        MockBukkit.unmock();
    }
}
