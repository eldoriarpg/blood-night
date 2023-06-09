package de.eldoria.bloodnight.nodes.transform.impl.construction;

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
import org.bukkit.World;

import java.util.Map;

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
@Meta(name = "Create Location", description = "Create a Location", category = Categories.CONSTRUCTION)
public final class CreateLocationNode extends TransformNode {
    @JsonCreator
    public CreateLocationNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public CreateLocationNode() {
    }

    @Override
    public OutputContainer output() {
        World world = input().value(Fields.WORLD);
        double x = input().map(Fields.X).asDouble();
        double y = input().map(Fields.Y).asDouble();
        double z = input().map(Fields.Z).asDouble();
        float yaw = input().map(Fields.YAW).asFloat();
        float pitch = input().map(Fields.PITCH).asFloat();
        return super.output().set(Fields.RESULT, new Location(world, x, y, z, yaw, pitch));
    }
}
