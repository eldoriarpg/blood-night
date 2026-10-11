package de.eldoria.bloodnight.nodes.serialization;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import de.eldoria.bloodnight.nodes.base.io.InputContainer;


public class InputContainerSerializer extends ValueSerializer<InputContainer> {
    @Override
    public void serialize(InputContainer value, JsonGenerator gen, SerializationContext serializers) {
        gen.writePOJO(value.edges());
    }
}
