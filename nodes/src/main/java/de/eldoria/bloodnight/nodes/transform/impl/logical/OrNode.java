package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

@Input(name = Fields.FIRST, type = DataType.BOOLEAN)
@Input(name = Fields.SECOND, type = DataType.BOOLEAN)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
public final class OrNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = (boolean) input().get(container, Fields.FIRST) || (boolean) input().get(container, Fields.SECOND);
        return super.output(container).set(Fields.RESULT, result);
    }
}
