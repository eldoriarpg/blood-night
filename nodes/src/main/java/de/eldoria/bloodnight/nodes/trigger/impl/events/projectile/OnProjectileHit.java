package de.eldoria.bloodnight.nodes.trigger.impl.events.projectile;

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
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnTeleportNode;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Map;

@Output(name = OnProjectileHit.Outputs.PROJECTILE, type = DataType.PROJECTILE)
@Output(name = OnTeleportNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
@Meta(name = "On teleport", description = "A trigger, that's called when an projectile hits another entity", category = Categories.EVENT)
public class OnProjectileHit extends CancelableEventTriggerNode<OnProjectileHit, ProjectileHitEvent> {
    @JsonCreator
    public OnProjectileHit(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return super.output(output)
                .set(Outputs.PROJECTILE, event.getEntity())
                .set(Outputs.CANCELABLE_EVENT, event);
    }

    public static class Outputs {
        public static final String PROJECTILE = Fields.PROJECTILE;
        public static final String CANCELABLE_EVENT = Fields.CANCELABLE_EVENT;
    }
}
