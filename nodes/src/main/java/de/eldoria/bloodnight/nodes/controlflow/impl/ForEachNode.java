package de.eldoria.bloodnight.nodes.controlflow.impl;

import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataStruct;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;

import java.util.Collection;

/**
 * An execution node executing other nodes for each entry in the provided list and providing the current element.
 */
@Input(name = Fields.VALUE, type = DataType.LINKED, struct = DataStruct.LIST)
@Output(name = Fields.CURRENT, type = DataType.LINKED, link = Fields.VALUE)
@Meta(name = "For Each", description = "executing other nodes for each entry in the provided list and providing the current element", category = Categories.DEFAULT)
public class ForEachNode extends ControlFlowNode<ForEachNode> {

    @Override
    public void invoke(NodeContainer container) {
        Collection<Object> collection = input().map(container, Fields.VALUE).get();
        for (var c : collection) {
            super.output(container).set(Fields.CURRENT, c);
            super.invoke(container);
        }
    }
}
