package de.eldoria.bloodnight.nodes.controlflow.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;

@Execution(Fields.NEXT)
@Input(name = Fields.VALUE, type = DataType.INTEGER)
public class BatchNode extends ControlFlowNode<BatchNode> {
    @Override
    public void invoke(NodeContainer container) {
        int count = input().map(container, Fields.VALUE).asInt();
        for (int i = 0; i < count; i++) {
            super.invoke(container);
        }
    }
}
