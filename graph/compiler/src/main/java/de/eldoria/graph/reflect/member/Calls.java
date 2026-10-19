/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect.member;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.definition.descriptor.NodeDescriptor;
import de.eldoria.graph.definition.descriptor.NodeKind;
import de.eldoria.graph.definition.emit.EmitContext;
import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.reflect.Documentation;
import de.eldoria.graph.reflect.Names;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Nodes that call an instance method on an object connected to {@code target}.
 * <p>
 * Reflection cannot tell whether a method changes something, so the consumer declares it: an action runs where it is
 * chained, and its return value, if any, is visible only to the chain after it; a query is a pure expression,
 * evaluated once per scope like a getter.
 */
public final class Calls {
    private static final String TARGET = "target";
    private static final String RESULT = "result";
    private static final String NEXT = "next";

    private Calls() {}

    /**
     * A method selected by a consumer.
     *
     * @param name   node id suffix, e.g. {@code add_potion_effect}
     * @param method the method name
     * @param params the parameters in order, named
     * @param action whether the call has side effects and runs in an execution chain
     */
    public record Call(String name, String method, List<Param> params, boolean action) {
        public Call {
            params = List.copyOf(params);
        }
    }

    /**
     * Builds the node for a call, named by {@code title}.
     *
     * @throws IllegalArgumentException when the method does not exist, is static, declares checked exceptions, uses
     *                                  type variables, or is a query without return value
     */
    public static NodeDefinition node(
            Class<?> type,
            String prefix,
            String category,
            String typeName,
            String title,
            Call call,
            Documentation docs) {
        Method method = method(type, call);
        Type[] generic = method.getGenericParameterTypes();
        boolean returns = method.getReturnType() != void.class;
        if (!call.action() && !returns) {
            throw new IllegalArgumentException("%s returns nothing and cannot be a query".formatted(method));
        }
        String description = docs.of(method).orElse("Calls %s on a %s".formatted(method.getName(), typeName));
        NodeDescriptor.Builder descriptor = NodeDescriptor.builder(
                        prefix + "." + call.name(), call.action() ? NodeKind.ACTION : NodeKind.PURE)
                .category(category)
                .name(title)
                .description(description)
                .input(TARGET, typeName, PortType.of(type), false, docs.of(type).orElse(""));
        for (int i = 0; i < generic.length; i++) {
            Param param = call.params().get(i);
            PortType input = declared(generic[i], param);
            descriptor.input(
                    param.name(),
                    Names.humanize(param.name()),
                    input,
                    false,
                    docs.parameter(method, i).orElse(param.description()));
        }
        if (returns) {
            PortType output = PortType.of(method.getGenericReturnType());
            if (output.hasVariables())
                throw new IllegalArgumentException("%s returns a type variable".formatted(method));
            descriptor.output(RESULT, "Result", output, docs.returns(method).orElse(""));
        }
        if (call.action()) descriptor.execution(NEXT);

        List<String> inputs = call.params().stream().map(Param::name).toList();
        String name = method.getName();
        if (!call.action()) {
            return new ExpressionDefinition(
                    descriptor.build(),
                    (port, ctx) -> CodeBlock.of("$L.$N($L)", ctx.input(TARGET), name, arguments(inputs, ctx)));
        }
        return new StatementDefinition(descriptor.build(), ctx -> {
            CodeBlock invocation = CodeBlock.of("$L.$N($L)", ctx.input(TARGET), name, arguments(inputs, ctx));
            CodeBlock.Builder code = CodeBlock.builder();
            if (returns && ctx.isRead(RESULT)) {
                CodeBlock result = ctx.bind(RESULT, NEXT);
                code.addStatement("final $T $L = $L", ctx.type(RESULT), result, invocation);
            } else {
                code.addStatement("$L", invocation);
            }
            return code.add(ctx.chain(NEXT)).build();
        });
    }

    private static CodeBlock arguments(List<String> inputs, EmitContext ctx) {
        return inputs.stream().map(ctx::input).collect(CodeBlock.joining(", "));
    }

    private static Method method(Class<?> type, Call call) {
        Class<?>[] types = call.params().stream().map(Param::type).toArray(Class<?>[]::new);
        Method method;
        try {
            method = type.getMethod(call.method(), types);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "%s has no public %s(%s)"
                            .formatted(
                                    type.getName(),
                                    call.method(),
                                    String.join(
                                            ", ",
                                            Arrays.stream(types)
                                                    .map(Class::getSimpleName)
                                                    .toList())),
                    e);
        }
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("%s is static; use a factory instead".formatted(method));
        }
        if (Arrays.stream(method.getExceptionTypes()).anyMatch(Calls::checked)) {
            throw new IllegalArgumentException("%s declares checked exceptions".formatted(method));
        }
        return method;
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
