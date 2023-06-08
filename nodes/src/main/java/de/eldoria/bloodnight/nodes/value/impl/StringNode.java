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
 * A node providing a {@link DataType#STRING}.
 */
@Output(name = Fields.VALUE, type = DataType.STRING)
@Meta(name = "String",description = "Allows to set a string value")
public final class StringNode extends ValueNode {
    public StringNode(String value) {
        super(value);
    }

    @JsonCreator
    public StringNode(@JsonProperty("value") String value, @JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(value, input, meta);
    }
}
