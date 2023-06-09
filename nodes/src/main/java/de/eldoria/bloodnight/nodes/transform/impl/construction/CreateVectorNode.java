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
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Node to construct a {@link Vector}.
 */
@Input(name = Fields.X, type = DataType.NUMBER)
@Input(name = Fields.Y, type = DataType.NUMBER)
@Input(name = Fields.Z, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.VECTOR)
@Meta(name = "Create Vector", description = "Create a Vector", category = Categories.CONSTRUCTION)
public final class CreateVectorNode extends TransformNode {
    @JsonCreator
    public CreateVectorNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public CreateVectorNode() {
    }

    @Override
    public OutputContainer output() {
        double x = input().map(Fields.X).asDouble();
        double y = input().map(Fields.Y).asDouble();
        double z = input().map(Fields.Z).asDouble();
        return super.output().set(Fields.RESULT, new Vector(x, y, z));
    }
}
