package de.eldoria.bloodnight.nodes.trigger.impl.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.Categories;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.trigger.base.CancelableEventTriggerNode;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.Map;

/**
 * A trigger, that's called when an entity ports to a new location
 */
@Output(name = OnTeleportNode.Outputs.FROM, type = DataType.LOCATION)
@Output(name = OnTeleportNode.Outputs.TO, type = DataType.LOCATION)
@Output(name = OnTeleportNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
@Meta(name = "On teleport", description = "A trigger, that's called when an entity ports to a new location", category = Categories.EVENT)
public class OnTeleportNode extends CancelableEventTriggerNode<OnTeleportNode, EntityTeleportEvent> {
    @JsonCreator
    public OnTeleportNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return super.output()
                .set(Outputs.FROM, event.getFrom())
                .set(Outputs.TO, event.getTo());
    }

    public static class Outputs {
        public static final String FROM = Fields.FROM;
        public static final String TO = Fields.TO;
        public static final String CANCELABLE_EVENT = Fields.CANCELABLE_EVENT;

    }
}
