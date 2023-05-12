package de.eldoria.bloodnight.nodes.action.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.annotations.Input;


@Input(name = Fields.VALUE, type = DataType.ANY)
public final class PrintNode extends ActionNode {

    @Override
    public void invoke(NodeContainer container) {
        System.out.println((Object) input().get(container, Fields.VALUE));
    }
}
