package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.meta.DataStruct;
import de.eldoria.bloodnight.nodes.meta.DataType;

public record InputMeta(String name, DataType type, DataStruct struct) {
    public static InputMeta fromAnnotation(Input input) {
        return new InputMeta(input.name(), input.type(), input.struct());
    }
}
