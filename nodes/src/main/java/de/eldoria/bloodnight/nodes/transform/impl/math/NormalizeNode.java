package de.eldoria.bloodnight.nodes.transform.impl.math;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.util.Vector;

/**
 * Normalize a vector. A normalized vector has the length 1.
 */
@Input(name = Fields.VALUE, type = DataType.VECTOR)
@Output(name = Fields.RESULT, type = DataType.VECTOR)
public class NormalizeNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Vector value = input().value(container, Fields.VALUE);
        return super.output(container).set(Fields.RESULT, value.normalize());
    }
}
