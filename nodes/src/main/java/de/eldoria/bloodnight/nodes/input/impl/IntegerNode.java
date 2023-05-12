package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.input.ValueNode;

@Output(name = "value", type = DataType.INTEGER)
public final class IntegerNode extends ValueNode {
    public IntegerNode(int value) {
        super(value);
    }
}
