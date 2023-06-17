package de.eldoria.bloodnight.nodes.transform.impl.math;

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
 * Get the distance between two points. This uses a costly sqrt function.
 * <p>
 * Consider using {@link DistanceSqrtNode} instead.
 */
@Input(name = Fields.FIRST, type = DataType.VECTOR)
@Input(name = Fields.SECOND, type = DataType.VECTOR)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
@Meta(name = "Distance", description = "Get the distance between two points. This uses a costly sqrt function. Consider using the \"Distance Squared\" node.", category = Categories.MATH)
public final class DistanceNode extends TransformNode {
    @JsonCreator
    public DistanceNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public DistanceNode() {
    }

    @Override
    public OutputContainer output() {
        var result = ((Vector) input().value(Fields.FIRST)).distance(input().value(Fields.SECOND));
        return super.output().set(Fields.RESULT, result);
    }
}
