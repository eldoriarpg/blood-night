package de.eldoria.bloodnight.nodes.input.impl;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.OutputContainer;
import de.eldoria.bloodnight.nodes.input.ValueNode;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Input(name = "value", type = DataType.INTEGER)
@Output(name = "result", type = DataType.BOOLEAN)
public final class CooldownNode extends ValueNode {
    private transient Instant last = Instant.now();

    @Override
    public OutputContainer output(NodeContainer container) {
        int cooldown = input().get(container, "value");
        if (Instant.now().isAfter(last.plus(cooldown, ChronoUnit.SECONDS))) {
            last = Instant.now();
            return super.output(container).set("result", true);
        }
        return super.output(container).set("result", false);
    }
}
