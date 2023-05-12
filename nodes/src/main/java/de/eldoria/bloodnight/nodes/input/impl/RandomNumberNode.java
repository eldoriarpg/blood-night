package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.OutputContainer;
import de.eldoria.bloodnight.nodes.input.ValueNode;

import java.util.concurrent.ThreadLocalRandom;

@Output(name = "result", type = DataType.NUMBER)
public final class RandomNumberNode extends ValueNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        return super.output(container).set("result", ThreadLocalRandom.current().nextDouble());
    }
}
