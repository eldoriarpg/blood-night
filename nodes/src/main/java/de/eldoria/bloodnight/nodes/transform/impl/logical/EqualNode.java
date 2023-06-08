package de.eldoria.bloodnight.nodes.transform.impl.logical;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

/**
 * A node combining two object with an equals check.
 */
@Input(name = Fields.FIRST, type = DataType.ANY)
@Input(name = Fields.SECOND, type = DataType.ANY)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
@Meta(name = "Equal", description = "Checks whether two object are equal")
public final class EqualNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = input().value(container, Fields.FIRST).equals(input().value(container, Fields.SECOND));
        return super.output(container).set(Fields.RESULT, result);
    }
}
