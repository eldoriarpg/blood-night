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
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.Map;

/**
 * A trigger, that's called when an entity got killed by this mob.
 */
@Output(name = OnTargetChangeNode.Outputs.TARGET, type = DataType.ENTITY)
@Output(name = OnTargetChangeNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
@Meta(name = "On target change", description = "A trigger, that's called when an entity looses or gets a new target.", category = Categories.EVENT)
public class OnTargetChangeNode extends CancelableEventTriggerNode<OnTargetChangeNode, EntityTargetEvent> {

    @JsonCreator
    public OnTargetChangeNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return super.output()
                .set(Outputs.TARGET, event.getEntity())
                .set(Outputs.CANCELABLE_EVENT, event);
    }

    public static class Outputs {
        public static final String TARGET = Fields.TARGET;
        public static final String CANCELABLE_EVENT = Fields.CANCELABLE_EVENT;
    }
}

