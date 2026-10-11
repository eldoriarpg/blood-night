package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import de.eldoria.bloodnight.nodes.StubValueNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SplitEntityNodeTest {

    @Test
    void output() {
        ServerMock mock = MockBukkit.mock();
        World world = mock.createWorld(new WorldCreator("world"));
        Location location = new Location(world, 1, 2, 3);
        Entity entity = world.spawnEntity(location, EntityType.ALLAY);

        NodeContainer nodeContainer = new NodeContainer();
        nodeContainer.add(1, new StubValueNode(entity, DataType.ENTITY));
        var splitEntityNode = nodeContainer.add(2, new SplitEntityNode());
        splitEntityNode.input().connect(Fields.VALUE, new Edge(1, Fields.VALUE));
        var output = splitEntityNode.output();

        assertEquals(EntityType.ALLAY, output.value(Fields.ENTITY_TYPE));
        assertEquals(entity.getLocation(), output.value(Fields.LOCATION));
        assertNull(output.value(Fields.VEHICLE));
        assertEquals(entity.getVelocity(), output.value(Fields.VELOCITY));
        assertEquals(DataType.ENTITY_TYPE, output.getType(Fields.ENTITY_TYPE));
        assertEquals(DataType.LOCATION, output.getType(Fields.LOCATION));
        assertEquals(DataType.ENTITY, output.getType(Fields.VEHICLE));
        assertEquals(DataType.VECTOR, output.getType(Fields.VELOCITY));
    }

    @AfterEach
    void afterAll() {
        MockBukkit.unmock();
    }
}
