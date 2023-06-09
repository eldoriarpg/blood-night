package de.eldoria.bloodnight.nodes.value.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.value.ValueNode;

import java.util.Map;

/**
 * A node providing a {@link DataType#BOOLEAN}.
 */
@Output(name = Fields.VALUE, type = DataType.BOOLEAN)
@Meta(name = "Boolean", description = "Allows to set a boolean")
public final class BooleanNode extends ValueNode {
    public BooleanNode(boolean value) {
        super(value);
    }

    @JsonCreator
    public BooleanNode(@JsonProperty("value") boolean value, @JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(value, input, meta);
    }
}
