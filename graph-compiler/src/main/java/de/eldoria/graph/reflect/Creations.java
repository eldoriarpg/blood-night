/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Nodes that create objects through a constructor or a static factory method.
 */
final class Creations {
    private static final String RESULT = "result";

    private Creations() {}

    /**
     * A constructor or factory selected by a consumer.
     *
     * @param name   node id suffix, e.g. {@code create}
     * @param method name of the static factory method, or {@code null} for a constructor
     * @param params the parameters in order, named
     */
    record Creation(String name, @Nullable String method, List<Param> params) {
        Creation {
            params = List.copyOf(params);
        }
    }

    /**
     * @param type a record class
     * @return its canonical constructor, with the components' names as input ids
     */
    static Creation record(Class<?> type) {
        RecordComponent[] components = type.getRecordComponents();
        return new Creation(
                "create",
                null,
                Arrays.stream(components)
                        .map(component -> Param.of(Names.snakeCase(component.getName()), component.getType()))
                        .toList());
    }

    /**
     * Builds the node for a creation.
     *
     * @throws IllegalArgumentException when the constructor or method does not exist, is not usable from generated
     *                                  code, or uses type variables
     */
    static ExpressionDefinition node(
            Class<?> type, String prefix, String category, String typeName, Creation creation, Documentation docs) {
        Class<?>[] types = creation.params().stream().map(Param::type).toArray(Class<?>[]::new);
        Executable executable = executable(type, creation, types);
        if (Arrays.stream(executable.getExceptionTypes()).anyMatch(Creations::checked)) {
            throw new IllegalArgumentException("%s declares checked exceptions".formatted(executable));
        }
        PortType output = creation.method() == null
                ? PortType.of(type)
                : PortType.of(((Method) executable).getGenericReturnType());
        Type[] generic = executable.getGenericParameterTypes();
        String description = docs.of(executable)
                .orElse(
                        creation.method() == null
                                ? "Creates a " + typeName
                                : "Calls %s.%s".formatted(type.getSimpleName(), creation.method()));
        NodeDescriptor.Builder descriptor = NodeDescriptor.builder(prefix + "." + creation.name(), NodeKind.PURE)
                .category(category)
                .name("%s %s".formatted(Names.humanize(creation.name()), typeName))
                .description(description);
        for (int i = 0; i < generic.length; i++) {
            Param param = creation.params().get(i);
            PortType input = declared(generic[i], param);
            descriptor.input(param.name(), Names.humanize(param.name()), input, false, "");
        }
        if (output.hasVariables())
            throw new IllegalArgumentException("%s returns a type variable".formatted(executable));
        descriptor.output(RESULT, typeName, output, "");
        List<String> inputs = creation.params().stream().map(Param::name).toList();
        String method = creation.method();
        return new ExpressionDefinition(descriptor.build(), (port, ctx) -> {
            CodeBlock arguments = inputs.stream().map(ctx::input).collect(CodeBlock.joining(", "));
            return method == null
                    ? CodeBlock.of("new $T($L)", type, arguments)
                    : CodeBlock.of("$T.$N($L)", type, method, arguments);
        });
    }

    private static Executable executable(Class<?> type, Creation creation, Class<?>[] types) {
        String method = creation.method();
        try {
            if (method == null) {
                if (Modifier.isAbstract(type.getModifiers())) {
                    throw new IllegalArgumentException(
                            "%s is abstract and cannot be constructed".formatted(type.getName()));
                }
                Constructor<?> constructor = type.getConstructor(types);
                return constructor;
            }
            Method factory = type.getMethod(method, types);
            if (!Modifier.isStatic(factory.getModifiers()) || factory.getReturnType() == void.class) {
                throw new IllegalArgumentException("%s is not a static factory".formatted(factory));
            }
            return factory;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "%s has no public %s(%s)"
                            .formatted(
                                    type.getName(),
                                    method == null ? "constructor" : method,
                                    String.join(
                                            ", ",
                                            Arrays.stream(types)
                                                    .map(Class::getSimpleName)
                                                    .toList())),
                    e);
        }
    }

    /**
     * The port type of a parameter: its generic type, or the type the consumer declared when the parameter is a type
     * variable, e.g. the {@code T data} of {@code World.spawnParticle}.
     */
    private static PortType declared(Type generic, Param param) {
        PortType type = PortType.of(generic);
        return type.hasVariables() ? PortType.of(param.type()) : type;
    }

    private static boolean checked(Class<?> exception) {
        return !RuntimeException.class.isAssignableFrom(exception) && !Error.class.isAssignableFrom(exception);
    }
}
