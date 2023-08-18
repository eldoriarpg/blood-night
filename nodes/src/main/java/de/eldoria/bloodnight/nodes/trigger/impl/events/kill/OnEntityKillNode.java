package de.eldoria.bloodnight.nodes.trigger.impl.events.kill;

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
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

/**
 * A trigger, that's called when an entity got killed by this mob.
 */
@Output(name = OnEntityKillNode.Outputs.KILLED_ENTITY, type = DataType.ENTITY)
@Output(name = OnEntityKillNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
@Meta(name = "On entity kill", description = "A trigger, that's called when an entity got killed by this mob.", category = Categories.EVENT)
public class OnEntityKillNode extends CancelableEventTriggerNode<OnEntityKillNode, EntityDamageByEntityEvent> {

    @JsonCreator
    public OnEntityKillNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return super.output()
                .set(Outputs.KILLED_ENTITY, event.getEntity())
                .set(OnPlayerKillNode.Outputs.CANCELABLE_EVENT, event);
    }

    public static class Outputs {
        public static final String KILLED_ENTITY = Fields.KILLED_ENTITY;
        public static final String CANCELABLE_EVENT = Fields.CANCELABLE_EVENT;
    }
}

