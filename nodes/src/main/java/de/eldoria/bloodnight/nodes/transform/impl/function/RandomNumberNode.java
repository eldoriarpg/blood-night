package de.eldoria.bloodnight.nodes.transform.impl.function;

import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A node providing a {@link DataType#NUMBER} within bounds.
 */
@Input(name = Fields.LOWER, type = DataType.NUMBER)
@Input(name = Fields.UPPER, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
@Meta(name = "Random Number", description = "Provides a random number within bounds.", category = Categories.FUNCTION)
public final class RandomNumberNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        double result = ThreadLocalRandom.current().nextDouble(input().map(container, Fields.LOWER).asDouble(), input().map(container, Fields.UPPER).asDouble());
        return super.output(container).set(Fields.RESULT, result);
    }
}
