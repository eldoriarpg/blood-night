package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

/**
 * A node inverting a boolean value.
 */
@Input(name = Fields.FIRST, type = DataType.BOOLEAN)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
public final class NotNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = !(boolean) input().value(container, Fields.FIRST);
        return super.output(container).set(Fields.RESULT, result);
    }
}
