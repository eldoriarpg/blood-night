package de.eldoria.bloodnight.nodes.transform.impl.logical;

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
 * A node combining two booleans with an {@code AND} operator.
 */
@Input(name = Fields.FIRST, type = DataType.BOOLEAN)
@Input(name = Fields.SECOND, type = DataType.BOOLEAN)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
@Meta(name = "and", description = "Combines two booleans with an AND operator", category = Categories.LOGICAL)
public final class AndNode extends TransformNode {
    @JsonCreator
    public AndNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public AndNode() {
    }

    @Override
    public OutputContainer output() {
        var result = (boolean) input().value(Fields.FIRST) && (boolean) input().value(Fields.SECOND);
        return super.output().set(Fields.RESULT, result);
    }
}
