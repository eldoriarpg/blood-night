package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.input.ValueNode;

/**
 * A node providing a {@link DataType#STRING}.
 */
@Output(name = Fields.VALUE, type = DataType.STRING)
public final class StringNode extends ValueNode {
    public StringNode(String value) {
        super(value);
    }
}
