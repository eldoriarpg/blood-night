package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Output;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MetadataReader {

    public static <T> Map<String, DataType> readInputs(Class<? extends T> klass) {
        return Arrays.stream(klass.getAnnotationsByType(Input.class))
                .collect(Collectors.toUnmodifiableMap(Input::name, Input::type));
    }

    public static <T> Map<String, DataType> readOutputs(Class<? extends T> klass) {
        return Arrays.stream(klass.getAnnotationsByType(Output.class))
                .collect(Collectors.toUnmodifiableMap(Output::name, Output::type));
    }

    public static <T> Map<String, Set<Integer>> readExecutions(Class<? extends T> klass) {
        return Arrays.stream(klass.getAnnotationsByType(Execution.class))
                .collect(Collectors.toUnmodifiableMap(Execution::value, k -> new HashSet<>()));
    }
}
