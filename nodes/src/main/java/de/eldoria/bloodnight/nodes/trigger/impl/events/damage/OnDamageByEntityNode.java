package de.eldoria.bloodnight.nodes.trigger.impl.events.damage;

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
@Output(name = OnDamageByEntityNode.Outputs.TARGET, type = DataType.PLAYER)
@Output(name = OnDamageByEntityNode.Outputs.DAMAGE, type = DataType.NUMBER)
@Output(name = OnDamageByEntityNode.Outputs.FINAL_DAMAGE, type = DataType.NUMBER)
@Output(name = OnDamageByEntityNode.Outputs.DAMAGE_CAUSE, type = DataType.DAMAGE_CAUSE)
@Meta(name = "On damage by entity", description = "A trigger, that's called when damaged was received from an entity.", category = Categories.EVENT)
public class OnDamageByEntityNode extends CancelableEventTriggerNode<OnDamageByEntityNode, EntityDamageByEntityEvent> {

    @JsonCreator
    public OnDamageByEntityNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return output.set(Outputs.TARGET, event.getEntity())
                .set(Outputs.DAMAGE, event.getDamage())
                .set(Outputs.FINAL_DAMAGE, event.getFinalDamage())
                .set(Outputs.DAMAGE_CAUSE, event.getCause());
    }

    public static class Outputs {
        public static final String TARGET = Fields.TARGET;
        public static final String DAMAGE = Fields.DAMAGE;
        public static final String FINAL_DAMAGE = Fields.FINAL_DAMAGE;
        public static final String DAMAGE_CAUSE = Fields.DAMAGE_CAUSE;
    }
}

