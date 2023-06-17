package de.eldoria.bloodnight.nodes.transform.impl.math;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.meta.Categories;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

import java.util.Map;

/**
 * Round a number to the closes integer value.
 */
@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.INTEGER)
@Meta(name = "Round", description = "Round a number to the closes integer value", category = Categories.MATH)
public final class RoundNode extends TransformNode {
    @JsonCreator
    public RoundNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public RoundNode() {
    }

    @Override
    public OutputContainer output() {
        var result = (int) Math.round(input().map(Fields.VALUE).asDouble());
        return super.output().set(Fields.RESULT, result);
    }
}
