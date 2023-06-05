package de.eldoria.bloodnight.nodes.value.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.value.ValueNode;

/**
 * A node providing a {@link DataType#NUMBER}.
 */
@Output(name = Fields.VALUE, type = DataType.NUMBER)
public final class NumberNode extends ValueNode {
    public NumberNode(double value) {
        super(value);
    }
}
