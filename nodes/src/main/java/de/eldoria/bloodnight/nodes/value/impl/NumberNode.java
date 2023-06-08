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
 * A node providing a {@link DataType#NUMBER}.
 */
@Output(name = Fields.VALUE, type = DataType.NUMBER)
@Meta(name = "Number",description = "Allows to set a decimal number")
public final class NumberNode extends ValueNode {
    public NumberNode(double value) {
        super(value);
    }

    @JsonCreator
    public NumberNode(double value, @JsonProperty Map<String, Edge> input, EditorMeta meta) {
        super(value, input, meta);
    }
}
