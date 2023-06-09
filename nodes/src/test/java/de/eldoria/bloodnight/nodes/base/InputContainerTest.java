package de.eldoria.bloodnight.nodes.base;

import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.InputContainer;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class InputContainerTest {

    @Test
    void test() {
        NodeContainer nodeContainer = new NodeContainer();
        InputContainer inputContainer = new InputContainer(Map.of("value", DataType.INTEGER));
        nodeContainer.add(1, new IntegerNode(1));
        inputContainer.inject(nodeContainer);
        inputContainer.connect("value", new Edge(1, "value"));
        Assertions.assertEquals(1, (Integer) inputContainer.value("value"));
    }
}
