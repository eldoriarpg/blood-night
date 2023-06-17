package de.eldoria.bloodnight.nodes.transform.impl.logical;

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
 * A node combining two booleans with an {@code OR} operator.
 */
@Input(name = Fields.FIRST, type = DataType.BOOLEAN)
@Input(name = Fields.SECOND, type = DataType.BOOLEAN)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
@Meta(name = "Or", description = "Combines two booleans with an OR operator", category = Categories.LOGICAL)
public final class OrNode extends TransformNode {
    @JsonCreator
    public OrNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public OrNode() {
    }

    @Override
    public OutputContainer output() {
        var result = (boolean) input().value(Fields.FIRST) || (boolean) input().value(Fields.SECOND);
        return super.output().set(Fields.RESULT, result);
    }
}
