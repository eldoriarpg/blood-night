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
 * A node calling other node depending on the evaluated boolean value.
 */
@Input(name = Fields.VALUE, type = DataType.BOOLEAN)
@Execution(Fields.TRUE)
@Execution(Fields.FALSE)
@Meta(name = "Branch", description = "calling other node depending on the evaluated boolean value.")
public class BranchNode extends ControlFlowNode<BranchNode> {

    public BranchNode() {
    }

    @JsonCreator
    public BranchNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    public void invoke(NodeContainer container) {
        var expression = String.valueOf((boolean) input().value(container, Fields.VALUE));
        executions().invokeNext(container, expression);
    }
}
