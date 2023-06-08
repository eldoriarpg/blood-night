package de.eldoria.bloodnight.nodes.transform.impl.logical;

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

import java.util.Map;

/**
 * A node checking whether an object is null
 */
@Input(name = Fields.VALUE, type = DataType.ANY)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
@Meta(name = "Is Null", description = "Checking whether an object is null", category = Categories.LOGICAL)
public final class IsNullNode extends TransformNode {
    @JsonCreator
    public IsNullNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public IsNullNode() {
    }

    @Override
    public OutputContainer output(NodeContainer container) {
        Object result = input().value(container, Fields.VALUE);
        return super.output(container).set(Fields.RESULT, result == null);
    }
}
