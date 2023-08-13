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
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

@Output(name = OnPlayerKillNode.Outputs.KILLED_ENTITY, type = DataType.PLAYER)
@Output(name = OnPlayerKillNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
@Meta(name = "On entity kill", description = "A trigger, that's called when a player got killed by this mob.", category = Categories.EVENT)
public class OnPlayerKillNode extends CancelableEventTriggerNode<OnPlayerKillNode, EntityDamageByEntityEvent> {

    @JsonCreator
    public OnPlayerKillNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return super.output()
                .set(Outputs.KILLED_ENTITY, event.getEntity());
    }

    public static class Outputs {
        public static final String KILLED_ENTITY = Fields.KILLED_ENTITY;
        public static final String CANCELABLE_EVENT = Fields.CANCELABLE_EVENT;
    }
}

