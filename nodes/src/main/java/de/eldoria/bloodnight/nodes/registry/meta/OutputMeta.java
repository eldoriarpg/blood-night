package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.meta.DataStruct;
import de.eldoria.bloodnight.nodes.meta.DataType;

public record OutputMeta(String name, DataType dataType, String link, DataStruct struct) {
    public static OutputMeta fromAnnotation(Output output) {
        return new OutputMeta(output.name(), output.type(), output.link(), output.struct());
    }
}
