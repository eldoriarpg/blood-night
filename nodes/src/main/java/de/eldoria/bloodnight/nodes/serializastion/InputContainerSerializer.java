package de.eldoria.bloodnight.nodes.serializastion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.eldoria.bloodnight.nodes.base.io.InputContainer;

import java.io.IOException;

public class InputContainerSerializer extends JsonSerializer<InputContainer> {
    @Override
    public void serialize(InputContainer value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value.edges());
    }
}
