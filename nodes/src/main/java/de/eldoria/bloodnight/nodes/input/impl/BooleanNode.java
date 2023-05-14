package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.input.ValueNode;

@Output(name = Fields.VALUE, type = DataType.BOOLEAN)
public class BooleanNode extends ValueNode {
    public BooleanNode(boolean value) { super(value); }
}
