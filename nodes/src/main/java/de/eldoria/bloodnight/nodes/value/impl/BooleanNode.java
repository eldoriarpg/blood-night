package de.eldoria.bloodnight.nodes.value.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.value.ValueNode;

/**
 * A node providing a {@link DataType#BOOLEAN}.
 */
@Output(name = Fields.VALUE, type = DataType.BOOLEAN)
@NodeMeta(name = "Boolean", description = "Allows to set a boolean")
public class BooleanNode extends ValueNode {
    public BooleanNode(boolean value) {
        super(value);
    }
}
