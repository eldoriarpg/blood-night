package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.Location;

@Input(name = "location", type = DataType.LOCATION)
@Output(name = "world", type = DataType.WORLD)
@Output(name = "x", type = DataType.NUMBER)
@Output(name = "y", type = DataType.NUMBER)
@Output(name = "z", type = DataType.NUMBER)
public final class SplitLocationNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Location loc = input().get(container, "location");
        return output(container)
                .set("world", loc.getWorld())
                .set("x", loc.getX())
                .set("y", loc.getY())
                .set("z", loc.getZ());
    }
}
