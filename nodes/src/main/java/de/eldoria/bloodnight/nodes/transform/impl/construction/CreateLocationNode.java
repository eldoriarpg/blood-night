package de.eldoria.bloodnight.nodes.transform.impl.construction;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.Location;
import org.bukkit.World;

@Input(name = "world", type = DataType.WORLD)
@Input(name = "x", type = DataType.NUMBER)
@Input(name = "y", type = DataType.NUMBER)
@Input(name = "z", type = DataType.NUMBER)
@Output(name = "location", type = DataType.LOCATION)
public final class CreateLocationNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        World world = input().get(container, "world");
        double x = input().get(container, "world");
        double y = input().get(container, "world");
        double z = input().get(container, "world");
        return output(container).set("location", new Location(world, x, y, z));
    }
}
