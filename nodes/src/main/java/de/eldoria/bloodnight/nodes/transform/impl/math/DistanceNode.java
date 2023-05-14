package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.util.Vector;

@Input(name = Fields.FIRST, type = DataType.VECTOR)
@Input(name = Fields.SECOND, type = DataType.VECTOR)
@Output(name = Fields.RESULT, type = DataType.NUMBER)
public final class DistanceNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        var result = ((Vector) input().get(container, Fields.FIRST)).distance(input().get(container, Fields.SECOND));
        return super.output(container).set(Fields.RESULT, result);
    }
}
