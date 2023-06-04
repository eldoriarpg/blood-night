package de.eldoria.bloodnight.nodes.controlflow.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;

/**
 * A node calling other node depending on the evaluated boolean value.
 */
@Input(name = Fields.VALUE, type = DataType.BOOLEAN)
@Execution(Fields.TRUE)
@Execution(Fields.FALSE)
public class IfNode extends ControlFlowNode<IfNode> {

    @Override
    public void invoke(NodeContainer container) {
        var expression = String.valueOf((boolean) input().value(container, Fields.VALUE));
        executions().invokeNext(container, expression);
    }
}
