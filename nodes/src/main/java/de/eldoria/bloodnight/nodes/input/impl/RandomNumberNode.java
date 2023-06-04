package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.input.ValueNode;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A node providing a {@link DataType#NUMBER} within bounds..
 */
@Input(name = Fields.LOWER, type = DataType.NUMBER)
@Input(name = Fields.UPPER, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
public final class RandomNumberNode extends ValueNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        double result = ThreadLocalRandom.current().nextDouble(input().map(container, Fields.LOWER).asDouble(), input().map(container, Fields.UPPER).asDouble());
        return super.output(container).set(Fields.RESULT, result);
    }
}
