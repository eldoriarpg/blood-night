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
 * A node combining two object with an equals check.
 */
@Input(name = Fields.FIRST, type = DataType.ANY)
@Input(name = Fields.SECOND, type = DataType.ANY)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
@Meta(name = "Equal", description = "Checks whether two object are equal", category = Categories.LOGICAL)
public final class EqualNode extends TransformNode {
    @JsonCreator
    public EqualNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public EqualNode() {
    }

    @Override
    public OutputContainer output() {
        var result = input().value(Fields.FIRST).equals(input().value(Fields.SECOND));
        return super.output().set(Fields.RESULT, result);
    }
}
