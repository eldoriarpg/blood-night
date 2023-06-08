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
 * A node providing a {@link DataType#BOOLEAN}.
 */
@Output(name = Fields.VALUE, type = DataType.BOOLEAN)
@Meta(name = "Boolean", description = "Allows to set a boolean")
public class BooleanNode extends ValueNode {
    public BooleanNode(boolean value) {
        super(value);
    }

    @JsonCreator
    public BooleanNode(@JsonProperty boolean value, @JsonProperty Map<String, Edge> input, EditorMeta meta) {
        super(value, input, meta);
    }
}
