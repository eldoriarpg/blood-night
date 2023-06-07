package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.annotations.Execution;

public record ExecutionMeta(String name) {
    public static ExecutionMeta fromAnnotation(Execution execution){
        return new ExecutionMeta(execution.value());
    }
}
