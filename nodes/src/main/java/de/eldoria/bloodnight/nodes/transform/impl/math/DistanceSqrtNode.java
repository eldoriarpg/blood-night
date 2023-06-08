package de.eldoria.bloodnight.nodes.transform.impl.math;

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
 * Returns the squared distance between two points.
 */
@Input(name = Fields.FIRST, type = DataType.VECTOR)
@Input(name = Fields.SECOND, type = DataType.VECTOR)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
@Meta(name = "Distance Squared", description = "Squared distance between two points.", category = Categories.MATH)
public final class DistanceSqrtNode extends TransformNode {
    @JsonCreator
    public DistanceSqrtNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public DistanceSqrtNode() {
    }

    @Override
    public OutputContainer output(NodeContainer container) {
        var result = ((Vector) input().value(container, Fields.FIRST)).distanceSquared(input().value(container, Fields.SECOND));
        return super.output(container).set(Fields.RESULT, result);
    }
}
