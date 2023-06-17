package de.eldoria.bloodnight.nodes.transform.impl.function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.Categories;
import de.eldoria.bloodnight.nodes.meta.DataStruct;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import de.eldoria.bloodnight.util.Checks;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;

/**
 * Node to get a list of nearby {@link Entity}s around a {@link Location}.
 */
@Input(name = Fields.LOCATION, type = DataType.LOCATION)
@Input(name = Fields.X, type = DataType.NUMBER)
@Input(name = Fields.Y, type = DataType.NUMBER)
@Input(name = Fields.Z, type = DataType.NUMBER)
@Output(name = Fields.RESULT, type = DataType.ENTITY, struct = DataStruct.LIST)
@Meta(name = "Nearby Entities", description = "Get a list of nearby Entities around a Location", category = Categories.FUNCTION)
public class NearbyEntitiesNode extends TransformNode {
    @JsonCreator
    public NearbyEntitiesNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    public NearbyEntitiesNode() {
    }

    @Override
    public OutputContainer output() {
        Location location = input().value(Fields.LOCATION);
        double x = input().value(Fields.X);
        double y = input().value(Fields.Y);
        double z = input().value(Fields.Z);
        Checks.notNull(location.getWorld(), "World must be non null.");

        return super.output()
                .set(Fields.RESULT, location.getWorld().getNearbyEntities(location, x, y, z));
    }
}
