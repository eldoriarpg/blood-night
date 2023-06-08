package de.eldoria.bloodnight.nodes.value;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;

import java.util.Map;

/**
 * Base node for nodes providing values which are defined by the user.
 */
public abstract non-sealed class ValueNode extends Node {

    public ValueNode(Object value, Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
        output().set(Fields.VALUE, value);
    }

    public ValueNode(Object value) {
        output().set(Fields.VALUE, value);
    }

    @Override
    public OutputContainer output(NodeContainer container) {
        return output();
    }

    @JsonProperty
    public Object value() {
        return output().value(Fields.VALUE);
    }
}
