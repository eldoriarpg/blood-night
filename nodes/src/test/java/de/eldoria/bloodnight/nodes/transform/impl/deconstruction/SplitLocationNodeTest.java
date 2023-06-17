package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitLocationNodeTest {

    @Test
    void output() {
        ServerMock mock = MockBukkit.mock();
        World world = mock.createWorld(new WorldCreator("world"));

        NodeContainer nodeContainer = new NodeContainer();
        Location location = new Location(world, 1, 2, 3);
        nodeContainer.add(1, new StubValueNode(location, DataType.LOCATION));
        var splitLocationNode = new SplitLocationNode();
        splitLocationNode.input().connect(Fields.LOCATION, new Edge(1, Fields.VALUE));
        var output = splitLocationNode.output();

        assertEquals(world, output.value(Fields.WORLD));
        assertEquals(location.getX(), output.value(Fields.X));
        assertEquals(location.getY(), output.value(Fields.Y));
        assertEquals(location.getZ(), output.value(Fields.Z));
        assertEquals(location.getDirection(), output.value(Fields.DIRECTION));
        assertEquals(DataType.WORLD, output.getType(Fields.WORLD));
        assertEquals(DataType.NUMBER, output.getType(Fields.X));
        assertEquals(DataType.NUMBER, output.getType(Fields.Y));
        assertEquals(DataType.NUMBER, output.getType(Fields.Z));
        assertEquals(DataType.VECTOR, output.getType(Fields.DIRECTION));
    }

    @AfterEach
    void afterAll() {
        MockBukkit.unmock();
    }
}
