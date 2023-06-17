package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.Categories;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.entity.Entity;

import java.util.Map;

/**
 * Node to split an {@link Entity}.
 */
@Input(name = Fields.VALUE, type = DataType.ENTITY)
@Output(name = Fields.ENTITY_TYPE, type = DataType.ENTITY_TYPE)
@Output(name = Fields.LOCATION, type = DataType.LOCATION)
@Output(name = Fields.VEHICLE, type = DataType.ENTITY)
@Output(name = Fields.VELOCITY, type = DataType.VECTOR)
@Meta(name = "Split Entity", description = "Split an entity", category = Categories.DECONSTRUCTION)
public final class SplitEntityNode extends TransformNode {
    @JsonCreator
    public SplitEntityNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public SplitEntityNode() {
    }

    @Override
    public OutputContainer output() {
        Entity entity = input().value(Fields.VALUE);
        return super.output()
                .set(Fields.ENTITY_TYPE, entity.getType())
                .set(Fields.LOCATION, entity.getLocation())
                .set(Fields.VEHICLE, entity.getVehicle())
                .set(Fields.VELOCITY, entity.getVelocity());
    }
}
