package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

@Input(name = Fields.VALUE, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
public final class AbsNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = Math.abs(input().map(container, Fields.VALUE).asDouble());
        return super.output(container).set(Fields.RESULT, result);
    }
}
