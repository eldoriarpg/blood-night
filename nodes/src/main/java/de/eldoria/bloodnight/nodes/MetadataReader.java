package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.registry.NodeRegistrationMeta;
import de.eldoria.bloodnight.nodes.registry.NodeType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class used to read metadata from class annotations.
 */
public final class MetadataReader {

    public static <T extends Node> Map<String, DataType> readInputs(Class<T> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Input.class))
                .collect(Collectors.toUnmodifiableMap(Input::name, Input::type));
    }

    public static <T extends Node> Map<String, DataType> readOutputs(Class<T> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Output.class))
                .collect(Collectors.toUnmodifiableMap(Output::name, Output::type));
    }

    public static <T extends Node> Map<String, Set<Integer>> readExecutions(Class<T> nodeClazz) {
        return Arrays.stream(nodeClazz.getAnnotationsByType(Execution.class))
                .collect(Collectors.toUnmodifiableMap(Execution::value, k -> new HashSet<>()));
    }

    public static <T extends Node> NodeRegistrationMeta readNodeMeta(Class<T> nodeClazz) {
        NodeMeta meta = nodeClazz.getAnnotation(NodeMeta.class);
        NodeType nodeType = NodeType.getNodeType(nodeClazz);
        return new NodeRegistrationMeta(meta.name(), meta.description(), nodeType, meta.category());
    }
}
