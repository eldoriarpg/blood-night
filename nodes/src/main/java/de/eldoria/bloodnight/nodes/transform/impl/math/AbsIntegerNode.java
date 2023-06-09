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
 * A node returning the absolute value of a number as integer
 */
@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.INTEGER)
@Meta(name = "Abs Integer", description = "Absolute value of a number as integer", category = Categories.MATH)
public final class AbsIntegerNode extends TransformNode {
    @JsonCreator
    public AbsIntegerNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public AbsIntegerNode() {
    }

    @Override
    public OutputContainer output() {
        var result = Math.abs(input().map(Fields.VALUE).asInt());
        return super.output().set(Fields.RESULT, result);
    }
}
