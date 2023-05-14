package de.eldoria.bloodnight.nodes.transform.impl.deconstruction;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import org.bukkit.entity.Entity;

@Input(name = Fields.VALUE, type = DataType.ENTITY)
@Output(name = Fields.ENTITY_TYPE, type = DataType.ENTITY_TYPE)
@Output(name = Fields.LOCATION, type = DataType.LOCATION)
@Output(name = Fields.VEHICLE, type = DataType.ENTITY)
@Output(name = Fields.VELOCITY, type = DataType.VECTOR)
public final class SplitEntityNode extends TransformNode {
    @Override
    public OutputContainer output(NodeContainer container) {
        Entity entity = input().get(container, Fields.VALUE);
        return output(container)
                .set(Fields.ENTITY_TYPE, entity.getType())
                .set(Fields.LOCATION, entity.getLocation())
                .set(Fields.VEHICLE, entity.getVehicle())
                .set(Fields.VELOCITY, entity.getVelocity());
    }
}
