package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Node to split a {@link Vector}.
 */
@Input(name = Fields.VECTOR, type = DataType.VECTOR)
@Output(name = Fields.X, type = DataType.NUMBER)
@Output(name = Fields.Y, type = DataType.NUMBER)
@Output(name = Fields.Z, type = DataType.NUMBER)
@NodeMeta(name = "Split Vector", description = "Split a Vector",category = Categories.DECONSTRUCTION)
public final class SplitVectorNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Vector loc = input().value(container, Fields.VECTOR);
        return super.output(container)
                .set(Fields.X, loc.getX())
                .set(Fields.Y, loc.getY())
                .set(Fields.Z, loc.getZ());
    }
}
