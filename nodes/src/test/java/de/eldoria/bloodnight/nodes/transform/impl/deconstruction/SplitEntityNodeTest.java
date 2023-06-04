package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitEntityNodeTest {

    @Test
    void output() {
        ServerMock mock = MockBukkit.mock();
        World world = mock.createWorld(new WorldCreator("world"));
        Location location = new Location(world, 1, 2, 3);
        Entity entity = world.spawnEntity(location, EntityType.ALLAY);

        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new StubValueNode(entity, DataType.ENTITY));
        var splitEntityNode = new SplitEntityNode();
        splitEntityNode.input().connect(nodeContainer, Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = splitEntityNode.output(nodeContainer);

        assertEquals(EntityType.ALLAY, output.value(Fields.ENTITY_TYPE));
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
