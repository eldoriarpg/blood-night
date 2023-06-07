package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.DataStruct;
import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.annotations.Input;

public record InputMeta(String name, DataType type, DataStruct struct) {
    public static InputMeta fromAnnotation(Input input){
        return new InputMeta(input.name(), input.type(), input.struct());
    }
}
