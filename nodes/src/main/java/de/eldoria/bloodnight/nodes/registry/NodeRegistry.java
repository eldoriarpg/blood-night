package de.eldoria.bloodnight.nodes.registry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.MetadataReader;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.registry.meta.ExecutionMeta;
import de.eldoria.bloodnight.nodes.registry.meta.InputMeta;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistration;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistrationMeta;
import de.eldoria.bloodnight.nodes.registry.meta.NodeType;
import de.eldoria.bloodnight.nodes.registry.meta.OutputMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NodeRegistry {
    static Map<String, NodeRegistration> nodes = new HashMap<>();
    static Set<String> names = new HashSet<>();

    public static void register(List<Class<? extends Node>> nodeClasses) {
        for (Class<? extends Node> nodeClass : nodeClasses) {
            register(nodeClass);
        }
    }

    public static void register(Class<? extends Node>... nodeClasses) {
        for (Class<? extends Node> nodeClass : nodeClasses) {
            register(nodeClass);
        }
    }

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

    public static List<NodeRegistration> registrations() {
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
        NodeType nodeType = NodeType.getNodeType(nodeClass);

        checkNodeMeta(nodeClass);

        checkNodeIO(nodeClass);

        // Check for correct serialization setup
        var jsonCreator = Arrays.stream(nodeClass.getConstructors()).filter(c -> c.getAnnotation(JsonCreator.class) != null).findAny();
        if (jsonCreator.isEmpty()) {
            throw new IllegalNodeState(nodeClass, "Missing constructor with @JsonCreator annotation.");
        }

        Constructor<? extends Node> constructor = (Constructor<? extends Node>) jsonCreator.get();

        checkJsonInputParameter(constructor, Map.class, "input");
        checkJsonInputParameter(constructor, EditorMeta.class, "meta");

        if (nodeType == NodeType.VALUE) {
            checkValueInputParameter(constructor);
            if (constructor.getParameterCount() != 3) {
                throw new IllegalNodeState(nodeClass, "Node of type VALUE requires 3 parameters, got %s.".formatted(constructor.getParameterCount()));
            }
        } else {
            if (constructor.getParameterCount() != 2) {
                throw new IllegalNodeState(nodeClass, "Node of type %s requires 2 parameters, got %s.".formatted(nodeType, constructor.getParameterCount()));
            }
        }
    }

    private static void checkNodeIO(Class<? extends Node> nodeClass) {
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

    private static void checkNodeMeta(Class<? extends Node> nodeClass) {
        Meta meta = nodeClass.getAnnotation(Meta.class);
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
    }

    private static void checkJsonInputParameter(Constructor<? extends Node> constructor, Class<?> inputClass, String name) {
        for (Parameter parameter : constructor.getParameters()) {
            if (parameter.getType() != inputClass) continue;
            JsonProperty jsonProperty = parameter.getAnnotation(JsonProperty.class);
            if (jsonProperty == null) {
                throw new IllegalNodeState(constructor.getDeclaringClass(), "Missing @JsonProperty annotation on input of type %s".formatted(inputClass));
            }
            if (!jsonProperty.value().equals(name)) {
                throw new IllegalNodeState(constructor.getDeclaringClass(), "Expected name \"%s\" for input %s, but got \"%s\"".formatted(name, inputClass, jsonProperty.value()));
            }
            return;
        }
        throw new IllegalNodeState(constructor.getDeclaringClass(), "Class is missing input with name %s of type %s".formatted(name, inputClass));
    }

    private static void checkValueInputParameter(Constructor<? extends Node> constructor) {
        for (Parameter parameter : constructor.getParameters()) {
            JsonProperty jsonProperty = parameter.getAnnotation(JsonProperty.class);
            if (jsonProperty == null) {
                throw new IllegalNodeState(constructor.getDeclaringClass(), "Missing @JsonProperty annotation on input of type %s".formatted(parameter.getType()));
            }
            if (jsonProperty.value().equals("value")) {
                return;
            }
        }
        throw new IllegalNodeState(constructor.getDeclaringClass(), "Class is of type VALUE, but missing input with name \"value\"");
    }
}
