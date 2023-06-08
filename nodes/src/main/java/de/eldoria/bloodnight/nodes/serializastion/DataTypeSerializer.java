package de.eldoria.bloodnight.nodes.serializastion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.eldoria.bloodnight.nodes.DataType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DataTypeSerializer extends JsonSerializer<DataType> {
    @Override
    public void serialize(DataType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(new DataTypeWrapper(value.name(), Arrays.stream(value.subtypes()).map(Enum::name).toList()));
    }

    private record DataTypeWrapper(String name, List<String> subtypes) {

    }
}
