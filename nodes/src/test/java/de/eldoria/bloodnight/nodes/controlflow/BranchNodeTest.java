package de.eldoria.bloodnight.nodes.controlflow;

import de.eldoria.bloodnight.nodes.DebugActionNode;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.controlflow.impl.BranchNode;
import de.eldoria.bloodnight.nodes.value.impl.BooleanNode;
import de.eldoria.bloodnight.nodes.trigger.impl.DebugNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BranchNodeTest {

    @ParameterizedTest
    @MethodSource("data")
    void testTrue(boolean condition, boolean firstBranch, boolean secondBranch) {
        var container = new NodeContainer();

        var firstNode = container.add(0, new DebugActionNode("true"));
        var secondNode = container.add(1, new DebugActionNode("false"));

        DebugNode debugNode = container.add(2, new DebugNode())
                .chain(Fields.NEXT, 3);

        container.add(4, new BooleanNode(condition));
        container.add(3, new BranchNode())
                .chain(Fields.TRUE, 0)
                .chain(Fields.FALSE, 1)
                .input().connect(Fields.VALUE, new Edge(4, Fields.VALUE));

        debugNode.invoke(container);

        assertEquals(firstBranch, firstNode.executed());
        assertEquals(secondBranch, secondNode.executed());
    }

    private static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(true, true, false),
                Arguments.of(false, false, true)
        );
    }
}
