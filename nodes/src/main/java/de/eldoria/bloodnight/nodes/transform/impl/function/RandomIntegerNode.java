package de.eldoria.bloodnight.nodes.transform.impl.function;

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
import java.util.concurrent.ThreadLocalRandom;

/**
 * A node providing a random {@link DataType#INTEGER} within inclusive bounds.
 */
@Input(name = Fields.LOWER, type = DataType.INTEGER)
@Input(name = Fields.UPPER, type = DataType.INTEGER)
@Output(name = Fields.RESULT, type = DataType.INTEGER)
@Meta(name = "Random Integer", description = "Provides a random integer within inclusive bounds.", category = Categories.FUNCTION)
public final class RandomIntegerNode extends TransformNode {
    @JsonCreator
    public RandomIntegerNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public RandomIntegerNode() {
    }

    @Override
    public OutputContainer output() {
        int result = ThreadLocalRandom.current().nextInt(input().map(Fields.LOWER).asInt(), input().map(Fields.UPPER).asInt() + 1);
        return super.output().set(Fields.RESULT, result);
    }
}
