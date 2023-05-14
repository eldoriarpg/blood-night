package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import de.eldoria.bloodnight.util.BMath;

@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Input(name = Fields.LOWER, type = DataType.NUMBER)
@Input(name = Fields.UPPER, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.INTEGER)
public final class ClampIntegerNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        int upper = input().map(container, Fields.UPPER).asInt();
        int lower = input().map(container, Fields.LOWER).asInt();
        int value = input().map(container, Fields.VALUE).asInt();
        return super.output(container).set(Fields.RESULT, BMath.clamp(value, lower, upper));
    }
}
