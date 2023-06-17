package de.eldoria.bloodnight.nodes.value.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.value.ValueNode;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * A node providing a {@link DataType#POTION_TYPE}.
 */
@Output(name = Fields.VALUE, type = DataType.POTION_TYPE)
@Meta(name = "Potion Type", description = "Allows to set a potion type")
public class PotionTypeNode extends ValueNode {
    public PotionTypeNode(@JsonProperty("value") PotionEffectType value, @JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(value, input, meta);
    }

}
