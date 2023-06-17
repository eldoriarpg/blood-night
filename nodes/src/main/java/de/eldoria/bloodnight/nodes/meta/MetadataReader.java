package de.eldoria.bloodnight.nodes.meta;

import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.registry.meta.ExecutionMeta;
import de.eldoria.bloodnight.nodes.registry.meta.InputMeta;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistrationMeta;
import de.eldoria.bloodnight.nodes.registry.meta.NodeType;
import de.eldoria.bloodnight.nodes.registry.meta.OutputMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class used to read metadata from class annotations.
 */
public final class MetadataReader {
    public static List<InputMeta> readInputMeta(Class<? extends Node> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Input.class))
                .map(InputMeta::fromAnnotation)
                .toList();
    }

    public static List<OutputMeta> readOutputMeta(Class<? extends Node> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Output.class))
                .map(OutputMeta::fromAnnotation)
                .toList();
    }

    public static List<ExecutionMeta> readExecutionMeta(Class<? extends Node> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Execution.class))
                .map(ExecutionMeta::fromAnnotation)
                .toList();
    }

    public static NodeRegistrationMeta readNodeMeta(Class<? extends Node> nodeClazz) {
        Meta meta = nodeClazz.getAnnotation(Meta.class);
        NodeType nodeType = NodeType.getNodeType(nodeClazz);
        return new NodeRegistrationMeta(meta.name(), meta.description(), nodeType, meta.category());
    }

    public static Map<String, DataType> readInputs(Class<? extends Node> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Input.class))
                .collect(Collectors.toUnmodifiableMap(Input::name, Input::type));
    }

    public static Map<String, DataType> readOutputs(Class<? extends Node> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Output.class))
                .collect(Collectors.toUnmodifiableMap(Output::name, Output::type));
    }

    public static Map<String, Set<Integer>> readExecutions(Class<? extends Node> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Execution.class))
                .collect(Collectors.toUnmodifiableMap(Execution::value, k -> new HashSet<>()));
    }
}
