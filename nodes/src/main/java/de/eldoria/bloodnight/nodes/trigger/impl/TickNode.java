package de.eldoria.bloodnight.nodes.trigger.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

import java.util.Map;

/**
 * Tick node is executed every server tick. Approx. every 50 ms.
 */
@Meta(name = "Tick", description = "Executed on every server tick")
public class TickNode extends TriggerNode<TickNode> {
        public TickNode() {
    }

        @JsonCreator
    public TickNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

}
