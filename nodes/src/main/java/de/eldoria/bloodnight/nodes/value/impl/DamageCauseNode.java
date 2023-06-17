package de.eldoria.bloodnight.nodes.value.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.value.ValueNode;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

/**
 * A node providing a {@link DataType#DAMAGE_CAUSE}.
 */
@Output(name = Fields.VALUE, type = DataType.DAMAGE_CAUSE)
@Meta(name = "Damage Cause", description = "Allows to set a damage cause")
public class DamageCauseNode extends ValueNode {

    @JsonCreator
    public DamageCauseNode(@JsonProperty("value") EntityDamageEvent.DamageCause value, @JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(value, input, meta);
    }
}
