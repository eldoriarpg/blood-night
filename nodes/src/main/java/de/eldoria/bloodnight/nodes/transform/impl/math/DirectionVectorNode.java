package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.util.Vector;

/**
 * Get the direction vector between two points.
 */
@Input(name = Fields.FROM, type = DataType.VECTOR)
@Input(name = Fields.TO, type = DataType.VECTOR)
@Output(name = Fields.RESULT, type = DataType.VECTOR)
@Meta(name = "Direction Vector", description = "Direction vector between two points")
public class DirectionVectorNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Vector from = input().value(container, Fields.FROM);
        Vector to = input().value(container, Fields.TO);
        var result = new Vector(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
        return super.output(container).set(Fields.RESULT, result);
    }
}
