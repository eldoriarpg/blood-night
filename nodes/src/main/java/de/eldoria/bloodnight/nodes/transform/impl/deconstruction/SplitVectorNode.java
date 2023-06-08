package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Node to split a {@link Vector}.
 */
@Input(name = Fields.VECTOR, type = DataType.VECTOR)
@Output(name = Fields.X, type = DataType.NUMBER)
@Output(name = Fields.Y, type = DataType.NUMBER)
@Output(name = Fields.Z, type = DataType.NUMBER)
@Meta(name = "Split Vector", description = "Split a Vector",category = Categories.DECONSTRUCTION)
public final class SplitVectorNode extends TransformNode {
    @JsonCreator
    public SplitVectorNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public SplitVectorNode() {
    }

    @Override
    public OutputContainer output(NodeContainer container) {
        Vector loc = input().value(container, Fields.VECTOR);
        return super.output(container)
                .set(Fields.X, loc.getX())
                .set(Fields.Y, loc.getY())
                .set(Fields.Z, loc.getZ());
    }
}
