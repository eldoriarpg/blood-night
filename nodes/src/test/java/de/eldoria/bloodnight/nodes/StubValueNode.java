package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.value.ValueNode;

import java.util.Map;

public class StubValueNode extends ValueNode {

    private final DataType type;

    public StubValueNode(Object value, DataType type) {
        super(value);
        this.type = type;
    }

    @Override
    public OutputContainer output() {
        return new OutputContainer(Map.of(Fields.VALUE, type))
                .set(Fields.VALUE, value());
    }
}
