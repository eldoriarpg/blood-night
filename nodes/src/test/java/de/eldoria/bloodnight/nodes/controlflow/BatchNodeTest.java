package de.eldoria.bloodnight.nodes.controlflow;

import de.eldoria.bloodnight.nodes.DebugActionNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.controlflow.impl.BatchNode;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.trigger.impl.DebugNode;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchNodeTest {

    @Test
    void invoke() {
        var container = new NodeContainer();
        var debugActionNode = container.add(0, new DebugActionNode("times executed"));

        container.add(1, new IntegerNode(69));

        BatchNode batchNode = container.add(2, new BatchNode());
        batchNode.input().connect(Fields.VALUE, new Edge(1, Fields.VALUE));
        batchNode.chain(Fields.NEXT, 0);

        DebugNode debugNode = container.add(3, new DebugNode());
        debugNode.chain(Fields.NEXT, 2)
                .invoke(container);

        assertEquals(debugActionNode.timesExecuted(), 69);
    }
}
