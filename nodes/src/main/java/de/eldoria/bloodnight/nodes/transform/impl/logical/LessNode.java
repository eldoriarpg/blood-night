package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

@Input(name = Fields.FIRST, type = DataType.NUMBER)
@Input(name = Fields.SECOND, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
public final class LessNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = (double) input().get(container, Fields.FIRST) < (double) input().get(container, Fields.SECOND);
        return super.output(container).set(Fields.RESULT, result);
    }
}
