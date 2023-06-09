package de.eldoria.bloodnight.nodes.transform.impl.construction;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import de.eldoria.bloodnight.nodes.value.impl.NumberNode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CreateLocationNodeTest {
    static World world;

    @BeforeAll
    static void beforeAll() {
        ServerMock mock = MockBukkit.mock();
        world = mock.createWorld(new WorldCreator("world"));

    }

    private static Stream<Arguments> outputData() {
        return Stream.of(
                Arguments.of(0, 0, 0, 0, 0),
                Arguments.of(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE / 2, 187, 187),
                Arguments.of(1000, 100, 10, 6.9F, 49F),
                Arguments.of(-1000, 100, 10, 0.4F, 0.07F)
        );
    }

    @AfterAll
    static void afterAll() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @MethodSource("outputData")
    void output(int x, int y, int z, float yaw, float pitch) {
        NodeContainer container = new NodeContainer();
        var createLocationNode = container.add(0, new CreateLocationNode());
        container.add(1, new IntegerNode(x));
        container.add(2, new IntegerNode(y));
        container.add(3, new IntegerNode(z));
        container.add(4, new NumberNode(yaw));
        container.add(5, new NumberNode(pitch));
        container.add(6, new StubValueNode(world, DataType.WORLD));
        createLocationNode.input()
                .connect(Fields.X, new Edge(1, Fields.VALUE))
                .connect(Fields.Y, new Edge(2, Fields.VALUE))
                .connect(Fields.Z, new Edge(3, Fields.VALUE))
                .connect(Fields.YAW, new Edge(4, Fields.VALUE))
                .connect(Fields.PITCH, new Edge(5, Fields.VALUE))
                .connect(Fields.WORLD, new Edge(6, Fields.VALUE));
        Assertions.assertEquals(new Location(world, x, y, z, yaw, pitch), createLocationNode.output().value(Fields.RESULT));
    }
}
