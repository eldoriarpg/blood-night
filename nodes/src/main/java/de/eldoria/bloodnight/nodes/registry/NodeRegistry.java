package de.eldoria.bloodnight.nodes.registry;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Inputs;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.annotations.Outputs;
import de.eldoria.bloodnight.nodes.base.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NodeRegistry {
    static Map<String, NodeRegistration> nodes = new HashMap<>();
    static Set<String> names = new HashSet<>();

    static {
        DefaultNodes.registerAll();
    }

    public static void register(Class<? extends Node> nodeClass) {
        bootstrapNode(nodeClass);
        nodes.put(nodeClass.getName(), generateRegistration(nodeClass));
    }

    private static NodeRegistration generateRegistration(Class<? extends Node> nodeClass) {
        // TODO: this needs to be meta instead of a simple data type map. We need to serialize the full annotation data
        Map<String, DataType> inputs = MetadataReader.readInputs(nodeClass);
        Map<String, DataType> outputs = MetadataReader.readOutputs(nodeClass);
        Map<String, Set<Integer>> executions = MetadataReader.readExecutions(nodeClass);
        NodeRegistrationMeta meta = MetadataReader.readNodeMeta(nodeClass);
        // TODO: Make order deterministic.
        return new NodeRegistration(nodeClass, meta, inputs, outputs, executions.keySet());
    }

    private static void bootstrapNode(Class<? extends Node> nodeClass) {
        NodeMeta meta = nodeClass.getAnnotation(NodeMeta.class);
        if (meta == null) {
            throw new IllegalNodeState(nodeClass, "Missing a NodeMeta annotation");
        }
        if (names.contains(meta.name().toLowerCase())) {
            throw new IllegalNodeState(nodeClass, "Name is already taken");
        }
        if (meta.description().isBlank()) {
            throw new IllegalNodeState(nodeClass, "Description is empty");
        }
        if (meta.category().isBlank()) {
            throw new IllegalNodeState(nodeClass, "Category is empty");
        }

        Inputs inputs = nodeClass.getAnnotation(Inputs.class);
        List<Input> inputFields = Collections.emptyList();
        if (inputs != null) {
            inputFields = List.of(inputs.value());
            for (Input input : inputFields) {
                if (input.name().isBlank()) {
                    throw new IllegalNodeState(nodeClass, "Input name is empty");
                }
            }
        }

        Outputs outputs = nodeClass.getAnnotation(Outputs.class);
        for (Output output : outputs.value()) {
            if (output.name().isBlank()) {
                throw new IllegalNodeState(nodeClass, "Output name is empty");
            }

            if (output.type() == DataType.LINKED) {
                if (inputFields.stream().filter(in -> in.name().equals(output.link())).findAny().isEmpty()) {
                    throw new IllegalNodeState(nodeClass, "Output field %s is linked to input field %s, but input does not exist".formatted(output.name(), output.link()));
                }
            } else {
                if (!output.link().isBlank()) {
                    throw new IllegalNodeState(nodeClass, "Output field %s defines a link, but type is not set to LINKED".formatted(output.name()));
                }
            }
        }
    }
}
