package de.eldoria.bloodnight.nodes.transform.impl.construction;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Node to construct a {@link Location}.
 */
@Input(name = Fields.WORLD, type = DataType.WORLD)
@Input(name = Fields.X, type = DataType.NUMBER)
@Input(name = Fields.Y, type = DataType.NUMBER)
@Input(name = Fields.Z, type = DataType.NUMBER)
@Input(name = Fields.YAW, type = DataType.NUMBER)
@Input(name = Fields.PITCH, type = DataType.NUMBER)
@Input(name = Fields.DIRECTION, type = DataType.VECTOR)
@Output(name = Fields.RESULT, type = DataType.LOCATION)
public final class CreateLocationNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        World world = input().value(container, Fields.WORLD);
        double x = input().map(container, Fields.X).asDouble();
        double y = input().map(container, Fields.Y).asDouble();
        double z = input().map(container, Fields.Z).asDouble();
        float yaw = input().map(container, Fields.YAW).asFloat();
        float pitch = input().map(container, Fields.PITCH).asFloat();
        return super.output(container).set(Fields.RESULT, new Location(world, x, y, z, yaw, pitch));
    }
}
