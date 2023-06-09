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
 * A node returning the next integer that is greater or equal to the value.
 */
@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.INTEGER)
@Meta(name = "Ceil", description = "Returns the next integer that is greater or equal to the value", category = Categories.MATH)
public final class CeilNode extends TransformNode {
    @JsonCreator
    public CeilNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public CeilNode() {
    }

    @Override
    public OutputContainer output() {
        var result = (int) Math.ceil(input().map(Fields.VALUE).asDouble());
        return super.output().set(Fields.RESULT, result);
    }
}
