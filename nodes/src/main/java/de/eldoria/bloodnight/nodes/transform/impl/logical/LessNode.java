package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

/**
 * A node checking whether a number is less than another number
 */@Input(name = Fields.FIRST, type = DataType.NUMBER)
@Input(name = Fields.SECOND, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
@NodeMeta(name = "Less", description = "Checking whether a number is less than another number")
public final class LessNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = (double) input().value(container, Fields.FIRST) < (double) input().value(container, Fields.SECOND);
        return super.output(container).set(Fields.RESULT, result);
    }
}
