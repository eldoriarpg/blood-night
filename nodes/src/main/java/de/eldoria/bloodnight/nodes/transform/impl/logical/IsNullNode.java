package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

@Input(name = Fields.VALUE, type = DataType.ANY)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
public final class IsNullNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Object result = input().get(container, Fields.VALUE);
        return super.output(container).set(Fields.RESULT, result == null);
    }
}
