package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

@Input(name = Fields.FIRST, type = DataType.NUMBER)
@Input(name = Fields.SECOND, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
public final class SubtractNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = ((Number) input().get(container, Fields.FIRST)).doubleValue() - ((Number) input().get(container, Fields.SECOND)).doubleValue();
        return super.output(container).set(Fields.RESULT, result);
    }
}
