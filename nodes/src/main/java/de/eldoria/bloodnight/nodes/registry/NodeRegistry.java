package de.eldoria.bloodnight.nodes.registry;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.registry.meta.ExecutionMeta;
import de.eldoria.bloodnight.nodes.registry.meta.InputMeta;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistration;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistrationMeta;
import de.eldoria.bloodnight.nodes.registry.meta.OutputMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NodeRegistry {
    static Map<String, NodeRegistration> nodes = new HashMap<>();
    static Set<String> names = new HashSet<>();

    public static void register(Class<? extends Node> nodeClass) {
        checkNode(nodeClass);
        NodeRegistration registration = generateRegistration(nodeClass);
        nodes.put(nodeClass.getName(), registration);
        names.add(registration.meta().name());
    }

    public static boolean unregisterNode(Class<? extends Node> nodeClass) {
        return unregisterNode(nodeClass.getName());
    }

    public static boolean unregisterNode(String name) {
        NodeRegistration remove = nodes.remove(name);
        if (remove != null) {
            names.remove(remove.meta().name());
        }
        return remove != null;
    }

    public static void unregisterAll() {
        nodes.clear();
        names.clear();
    }

    public static List<NodeRegistration> registrations(){
        return List.copyOf(nodes.values());
    }

    private static NodeRegistration generateRegistration(Class<? extends Node> nodeClass) {
        List<InputMeta> inputs = MetadataReader.readInputMeta(nodeClass);
        List<OutputMeta> outputs = MetadataReader.readOutputMeta(nodeClass);
        List<ExecutionMeta> executions = MetadataReader.readExecutionMeta(nodeClass);
        NodeRegistrationMeta meta = MetadataReader.readNodeMeta(nodeClass);
        return new NodeRegistration(nodeClass, meta, inputs, outputs, executions);
    }

    private static void checkNode(Class<? extends Node> nodeClass) {
        NodeMeta meta = nodeClass.getAnnotation(NodeMeta.class);
        if (meta == null) {
            throw new IllegalNodeState(nodeClass, "Missing a NodeMeta annotation");
        }
        if (meta.name().isBlank()) {
            throw new IllegalNodeState(nodeClass, "Description is empty");
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

        List<Input> inputs = List.of(nodeClass.getAnnotationsByType(Input.class));
        for (Input input : inputs) {
            if (input.name().isBlank()) {
                throw new IllegalNodeState(nodeClass, "Input name is empty");
            }
        }

        List<Output> outputs = List.of(nodeClass.getAnnotationsByType(Output.class));
        for (Output output : outputs) {
            if (output.name().isBlank()) {
                throw new IllegalNodeState(nodeClass, "Output name is empty");
            }

            if (output.type() == DataType.LINKED) {
                if (inputs.stream().filter(in -> in.name().equals(output.link())).findAny().isEmpty()) {
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
