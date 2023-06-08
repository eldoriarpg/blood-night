package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

/**
 * A node returning the absolute value of a number as integer
 */
@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.INTEGER)
@Meta(name = "Abs Integer", description = "Absolute value of a number as integer")
public final class AbsIntegerNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = Math.abs(input().map(container, Fields.VALUE).asInt());
        return super.output(container).set(Fields.RESULT, result);
    }
}
