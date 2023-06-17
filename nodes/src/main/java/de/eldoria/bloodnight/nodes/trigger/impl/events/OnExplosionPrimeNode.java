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
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.Map;

@Output(name = OnExplosionPrimeNode.Outputs.KILLED_ENTITY, type = DataType.ENTITY)
@Output(name = OnExplosionPrimeNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
@Meta(name = "On explosion prime", description = "A trigger, that's called when an entity decided to explode.", category = Categories.EVENT)
public class OnExplosionPrimeNode extends CancelableEventTriggerNode<OnExplosionPrimeNode, ExplosionPrimeEvent> {

    @JsonCreator
    public OnExplosionPrimeNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
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
