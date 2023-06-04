package de.eldoria.bloodnight.nodes.base;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.InputContainer;
import de.eldoria.bloodnight.nodes.input.impl.IntegerNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class InputContainerTest {

    @Test
    void test() {
        NodeContainer nodeContainer = new NodeContainer();
        InputContainer inputContainer = new InputContainer(Map.of("value", DataType.INTEGER));
        nodeContainer.add(1, new IntegerNode(1));
        inputContainer.connect(nodeContainer, "value", new Edge(1, "value"));
        Assertions.assertEquals(1, (Integer) inputContainer.value(nodeContainer, "value"));
    }
}
