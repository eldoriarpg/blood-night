package de.eldoria.bloodnight.nodes.transform.impl.math;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Normalize a vector. A normalized vector has the length 1.
 */
@Input(name = Fields.VALUE, type = DataType.VECTOR)
@Output(name = Fields.RESULT, type = DataType.VECTOR)
@Meta(name = "Normalize", description = "Normalize a vector. A normalized vector has the length 1.", category = Categories.MATH)
public class NormalizeNode extends TransformNode {
    @JsonCreator
    public NormalizeNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public NormalizeNode() {
    }

    @Override
    public OutputContainer output(NodeContainer container) {
        Vector value = input().value(container, Fields.VALUE);
        return super.output(container).set(Fields.RESULT, value.normalize());
    }
}
