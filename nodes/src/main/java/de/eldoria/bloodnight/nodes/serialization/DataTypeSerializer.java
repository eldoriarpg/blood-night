package de.eldoria.bloodnight.nodes.serialization;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import de.eldoria.bloodnight.nodes.meta.DataType;

import java.util.Arrays;
import java.util.List;

public class DataTypeSerializer extends ValueSerializer<DataType> {
    @Override
    public void serialize(DataType value, JsonGenerator gen, SerializationContext serializers) {
        gen.writePOJO(new DataTypeWrapper(value.name(), Arrays.stream(value.subtypes()).map(Enum::name).toList()));
    }

    private record DataTypeWrapper(String name, List<String> subtypes) {

    }
}
