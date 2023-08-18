package de.eldoria.bloodnight.nodes.trigger.impl.events.explosion;

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
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Map;

/**
 * A trigger, that's called when an entity is exploding.
 */
@Output(name = OnExplosionNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
@Meta(name = "On explosion", description = "A trigger, that's called when an entity is exploding.", category = Categories.EVENT)
public class OnExplosionNode extends CancelableEventTriggerNode<OnExplosionNode, EntityExplodeEvent> {

    @JsonCreator
    public OnExplosionNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return super.output()
                .set(Outputs.CANCELABLE_EVENT, event);
    }

    public static class Outputs {
        public static final String CANCELABLE_EVENT = Fields.CANCELABLE_EVENT;
    }
}
