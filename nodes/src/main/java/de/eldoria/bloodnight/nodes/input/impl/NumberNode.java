package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.input.ValueNode;

@Output(name = "value", type = DataType.NUMBER)
public final class NumberNode extends ValueNode {
    public NumberNode(double value) {
        super(value);
    }
}
