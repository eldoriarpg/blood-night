package de.eldoria.bloodnight.nodes.controlflow;

import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.trigger.impl.DebugNode;
import org.junit.jupiter.api.Test;

public class ForEachNodeTest {

    @Test
    void test() {
        var container = new NodeContainer();
        var debugNode = new DebugNode();
        container.add(1, debugNode);

    }
}
