package de.eldoria.bloodnight.nodes.transform.impl.function;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import de.eldoria.bloodnight.nodes.value.ValueNode;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A node providing a random {@link DataType#INTEGER} within inclusive bounds.
 */
@Input(name = Fields.LOWER, type = DataType.INTEGER)
@Input(name = Fields.UPPER, type = DataType.INTEGER)
@Output(name = Fields.RESULT, type = DataType.INTEGER)
public final class RandomIntegerNode extends TransformNode {

    @Override
    public OutputContainer output(NodeContainer container) {
        int result = ThreadLocalRandom.current().nextInt(input().map(container, Fields.LOWER).asInt(), input().map(container, Fields.UPPER).asInt() + 1);
        return super.output(container).set(Fields.RESULT, result);
    }
}
