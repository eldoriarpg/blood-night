package de.eldoria.bloodnight.nodes.controlflow.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;

import java.util.Map;

/**
 * An execution node executing nodes multiple times.
 */
@Execution(Fields.NEXT)
@Input(name = Fields.VALUE, type = DataType.INTEGER)
@Meta(name = "Batch", description = "Execute something multiple times")
public class BatchNode extends ControlFlowNode<BatchNode> {
    public BatchNode() {
    }

    @JsonCreator
    public BatchNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    public void invoke(NodeContainer container) {
        int count = input().map(container, Fields.VALUE).asInt();
        for (int i = 0; i < count; i++) {
            super.invoke(container);
        }
    }
}
