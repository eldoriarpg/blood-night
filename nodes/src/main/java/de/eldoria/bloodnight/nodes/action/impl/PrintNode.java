package de.eldoria.bloodnight.nodes.action.impl;

import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;

/**
 * A node which prints something to std out
 */
@Input(name = Fields.VALUE, type = DataType.ANY)
@Meta(name = "Print", description = "Prints something to the console", category = Categories.DEBUGGING)
public final class PrintNode extends ActionNode {

    @Override
    public void invoke(NodeContainer container) {
        System.out.println((Object) input().value(container, Fields.VALUE));
    }
}
