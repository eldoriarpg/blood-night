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
import de.eldoria.bloodnight.util.BMath;

import java.util.Map;

/**
 * Returns the value and ensures that it is within the upper and lower bound.
 */
@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Input(name = Fields.LOWER, type = DataType.NUMBER)
@Input(name = Fields.UPPER, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
@Meta(name = "Clamp", description = "Returns the value and ensures that it is within the upper and lower bound.", category = Categories.MATH)
public final class ClampNode extends TransformNode {
    @JsonCreator
    public ClampNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public ClampNode() {
    }

    @Override
    public OutputContainer output(NodeContainer container) {
        double upper = input().map(container, Fields.UPPER).asDouble();
        double lower = input().map(container, Fields.LOWER).asDouble();
        double value = input().map(container, Fields.VALUE).asDouble();
        return super.output(container).set(Fields.RESULT, BMath.clamp(value, lower, upper));
    }
}
