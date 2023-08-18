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
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.Map;

/**
 * A trigger, that's called when an shoots a projectile
 */
@Output(name = OnProjectileShoot.Outputs.PROJECTILE, type = DataType.PROJECTILE)
@Meta(name = "On projectile shoot", description = "A trigger, that's called when an shoots a projectile", category = Categories.EVENT)
@Output(name = OnTeleportNode.Outputs.CANCELABLE_EVENT, type = DataType.CANCELABLE_EVENT)
public class OnProjectileShoot extends CancelableEventTriggerNode<OnProjectileShoot, ProjectileLaunchEvent> {
    @JsonCreator
    public OnProjectileShoot(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return super.output(output)
                .set(Outputs.PROJECTILE, event.getEntity());
    }

    public static class Outputs {
        public static final String PROJECTILE = Fields.PROJECTILE;
        public static final String CANCELABLE_EVENT = Fields.CANCELABLE_EVENT;
    }
}
