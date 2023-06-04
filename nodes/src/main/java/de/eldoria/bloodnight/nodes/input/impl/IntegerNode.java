package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.input.ValueNode;

/**
 * A node providing an {@link DataType#INTEGER}.
 */
@Output(name = Fields.VALUE, type = DataType.INTEGER)
public final class IntegerNode extends ValueNode {
    public IntegerNode(int value) {
        super(value);
    }
}
