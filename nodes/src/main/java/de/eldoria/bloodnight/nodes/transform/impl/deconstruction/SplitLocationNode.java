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
import org.bukkit.entity.Entity;

/**
 * Node to split a {@link Location}.
 */
@Input(name = Fields.LOCATION, type = DataType.LOCATION)
@Output(name = Fields.WORLD, type = DataType.WORLD)
@Output(name = Fields.X, type = DataType.NUMBER)
@Output(name = Fields.Y, type = DataType.NUMBER)
@Output(name = Fields.Z, type = DataType.NUMBER)
@Output(name = Fields.DIRECTION, type = DataType.VECTOR)
@NodeMeta(name = "Split Location", description = "Split a Location",category = Categories.DECONSTRUCTION)
public final class SplitLocationNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Location loc = input().value(container, Fields.LOCATION);
        return super.output(container)
                .set(Fields.WORLD, loc.getWorld())
                .set(Fields.X, loc.getX())
                .set(Fields.Y, loc.getY())
                .set(Fields.Z, loc.getZ())
                .set(Fields.DIRECTION, loc.getDirection());
    }
}
