package de.eldoria.bloodnight.nodes.transform.impl.function;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import de.eldoria.bloodnight.nodes.value.ValueNode;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A node providing a {@link DataType#BOOLEAN} which is true every {@code n} milliseconds.
 */
@Input(name = Fields.VALUE, type = DataType.INTEGER)
@Output(name = Fields.RESULT, type = DataType.BOOLEAN)
public final class CooldownNode extends TransformNode {
    private transient Instant last = Instant.EPOCH;

    @Override
    public OutputContainer output(NodeContainer container) {
        int cooldown = input().map(container, Fields.VALUE).asInt();

        if (last.plus(cooldown, ChronoUnit.MILLIS).isBefore(Instant.now())) {
            last = Instant.now();
            return super.output(container).set(Fields.RESULT, true);
        }
        return super.output(container).set(Fields.RESULT, false);
    }
}
