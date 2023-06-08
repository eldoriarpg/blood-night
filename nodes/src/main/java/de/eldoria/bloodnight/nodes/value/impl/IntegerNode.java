package de.eldoria.bloodnight.nodes.value.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.value.ValueNode;

import java.util.Map;

/**
 * A node providing an {@link DataType#INTEGER}.
 */
@Output(name = Fields.VALUE, type = DataType.INTEGER)
@Meta(name = "Integer",description = "Allows to set an integer number")
public final class IntegerNode extends ValueNode {
    public IntegerNode(int value) {
        super(value);
    }

    @JsonCreator
    public IntegerNode(@JsonProperty int value, @JsonProperty Map<String, Edge> input, EditorMeta meta) {
        super(value, input, meta);
    }
}
