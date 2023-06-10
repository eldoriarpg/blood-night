package de.eldoria.bloodnight.nodes.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;

import java.util.Map;

/**
 * A node which prints something to std out
 */
@Input(name = Fields.VALUE, type = DataType.ANY)
@Meta(name = "Print", description = "Prints something to the console", category = Categories.DEBUGGING)
public final class PrintNode extends ActionNode {
    @JsonCreator
    public PrintNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public PrintNode() {
    }

    @Override
    public void invoke(NodeContainer container) {
        System.out.println((Object) input().value(container, Fields.VALUE));
    }

    public static class Input {
        public static final String VALUE = Fields.VALUE;
    }
}
