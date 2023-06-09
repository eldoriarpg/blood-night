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

import java.util.Map;

/**
 * A node returning the absolute value of a number.
 */
@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
@Meta(name = "Abs", description = "Absolute value of a number", category = Categories.MATH)
public final class AbsNode extends TransformNode {
    @JsonCreator
    public AbsNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public AbsNode() {
    }

    @Override
    public OutputContainer output() {
        var result = Math.abs(input().map(Fields.VALUE).asDouble());
        return super.output().set(Fields.RESULT, result);
    }
}
