package de.eldoria.bloodnight.nodes.transform.impl.function;

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
import java.util.concurrent.ThreadLocalRandom;

/**
 * A node providing a {@link DataType#NUMBER} within bounds.
 */
@Input(name = Fields.LOWER, type = DataType.NUMBER)
@Input(name = Fields.UPPER, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
@Meta(name = "Random Number", description = "Provides a random number within bounds.", category = Categories.FUNCTION)
public final class RandomNumberNode extends TransformNode {
    @JsonCreator
    public RandomNumberNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public RandomNumberNode() {
    }

    @Override
    public OutputContainer output() {
        double result = ThreadLocalRandom.current().nextDouble(input().map(Fields.LOWER).asDouble(), input().map(Fields.UPPER).asDouble());
        return super.output().set(Fields.RESULT, result);
    }
}
