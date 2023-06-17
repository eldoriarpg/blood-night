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
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;

@Output(name = OnDeathNode.Outputs.KILLER, type = DataType.PLAYER)
@Output(name = OnDeathNode.Outputs.DAMAGE_CAUSE, type = DataType.DAMAGE_CAUSE)
@Meta(name = "On entity death", description = "A trigger, that's called when an entity dies.", category = Categories.EVENT)
public class OnDeathNode extends TriggerNode<OnDeathNode, EntityDeathEvent> {

    private EntityDeathEvent event;

    @JsonCreator
    public OnDeathNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected void inject(EntityDeathEvent event) {
        this.event = event;
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return output
                .set(Outputs.KILLER, event.getEntity().getKiller())
                .set(Outputs.DAMAGE_CAUSE, event.getEntity().getLastDamageCause());
    }

    public static class Outputs {
        public static final String KILLER = Fields.KILLER;
        public static final String DAMAGE_CAUSE = Fields.DAMAGE_CAUSE;
    }
}
