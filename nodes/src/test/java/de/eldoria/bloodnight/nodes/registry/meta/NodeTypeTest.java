package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.action.impl.PrintNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.BranchNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.AbsNode;
import de.eldoria.bloodnight.nodes.trigger.impl.TickNode;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NodeTypeTest {

    @Test
    void getNodeType() {
        Assertions.assertEquals(NodeType.getNodeType(IntegerNode.class), NodeType.VALUE);
        Assertions.assertEquals(NodeType.getNodeType(BranchNode.class), NodeType.CONTROL_FLOW);
        Assertions.assertEquals(NodeType.getNodeType(PrintNode.class), NodeType.ACTION);
        Assertions.assertEquals(NodeType.getNodeType(AbsNode.class), NodeType.TRANSFORM);
        Assertions.assertEquals(NodeType.getNodeType(TickNode.class), NodeType.TRIGGER);
    }
}
