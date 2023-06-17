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
 * A node adding a value to another value.
 */
@Input(name = Fields.FIRST, type = DataType.NUMBER)
@Input(name = Fields.SECOND, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
@Meta(name = "Add", description = "Add a value to another value", category = Categories.MATH)
public final class AddNode extends TransformNode {
    @JsonCreator
    public AddNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public AddNode() {
    }

    @Override
    public OutputContainer output() {
        var result = input().map(Fields.FIRST).asDouble() + input().map(Fields.SECOND).asDouble();
        return super.output().set(Fields.RESULT, result);
    }

    public static class Input {
        public static final String FIRST = Fields.FIRST;
        public static final String SECOND = Fields.SECOND;
    }

    public static class Output {
        public static final String RESULT = Fields.RESULT;
    }
}
