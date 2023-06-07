package de.eldoria.bloodnight.nodes.controlflow.impl;

import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;

/**
 * An execution node executing nodes multiple times.
 */
@Execution(Fields.NEXT)
@Input(name = Fields.VALUE, type = DataType.INTEGER)
@NodeMeta(name = "Batch", description = "Execute something multiple times", category = Categories.DEFAULT)
public class BatchNode extends ControlFlowNode<BatchNode> {
    @Override
    public void invoke(NodeContainer container) {
        int count = input().map(container, Fields.VALUE).asInt();
        for (int i = 0; i < count; i++) {
            super.invoke(container);
        }
    }
}
