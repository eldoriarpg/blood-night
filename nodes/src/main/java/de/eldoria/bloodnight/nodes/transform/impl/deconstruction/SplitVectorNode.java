package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.util.Vector;

@Input(name = "vector", type = DataType.VECTOR)
@Output(name = "x", type = DataType.NUMBER)
@Output(name = "y", type = DataType.NUMBER)
@Output(name = "z", type = DataType.NUMBER)
public final class SplitVectorNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Vector loc = input().get(container, "vector");
        return output(container)
                .set("x", loc.getX())
                .set("y", loc.getY())
                .set("z", loc.getZ());
    }
}
