package de.eldoria.bloodnight.mob.serialization;

import java.util.function.Function;

public record NamedAttribute(String id, String name) {
    public <T> T map(Function<String, T> map) {
        return map.apply(id);
    }
}
