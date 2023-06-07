package de.eldoria.bloodnight.nodes.value.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.value.ValueNode;

/**
 * A node providing a {@link DataType#STRING}.
 */
@Output(name = Fields.VALUE, type = DataType.STRING)
@NodeMeta(name = "String",description = "Allows to set a string value")
public final class StringNode extends ValueNode {
    public StringNode(String value) {
        super(value);
    }
}
