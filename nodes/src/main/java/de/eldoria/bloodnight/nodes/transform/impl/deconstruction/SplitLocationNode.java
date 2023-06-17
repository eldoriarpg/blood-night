package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.Categories;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.Location;

import java.util.Map;

/**
 * Node to split a {@link Location}.
 */
@Input(name = Fields.LOCATION, type = DataType.LOCATION)
@Output(name = Fields.WORLD, type = DataType.WORLD)
@Output(name = Fields.X, type = DataType.NUMBER)
@Output(name = Fields.Y, type = DataType.NUMBER)
@Output(name = Fields.Z, type = DataType.NUMBER)
@Output(name = Fields.DIRECTION, type = DataType.VECTOR)
@Meta(name = "Split Location", description = "Split a Location", category = Categories.DECONSTRUCTION)
public final class SplitLocationNode extends TransformNode {
    @JsonCreator
    public SplitLocationNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public SplitLocationNode() {
    }

    @Override
    public OutputContainer output() {
        Location loc = input().value(Fields.LOCATION);
        return super.output()
                .set(Fields.WORLD, loc.getWorld())
                .set(Fields.X, loc.getX())
                .set(Fields.Y, loc.getY())
                .set(Fields.Z, loc.getZ())
                .set(Fields.DIRECTION, loc.getDirection());
    }
}
