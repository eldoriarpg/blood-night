package de.eldoria.bloodnight.nodes.trigger.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

import java.util.Map;

/**
 * Debugging node. Used for manual triggering of events.
 */
@Execution(Fields.NEXT)
@Meta(name = "Debug", description = "Manually trigger a action", category = Categories.DEBUGGING)
public final class DebugNode extends TriggerNode<DebugNode, Void> {
    public DebugNode() {
    }

    @JsonCreator
    public DebugNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected void inject(Void data) {
        // ignore
    }

}
