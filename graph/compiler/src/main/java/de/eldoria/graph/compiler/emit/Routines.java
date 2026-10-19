/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler.emit;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.definition.descriptor.NodeDescriptor;
import de.eldoria.graph.definition.descriptor.NodeKind;
import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.types.TypeMapping;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Routines: named chains of one graph with typed parameters, called from any chain of the same graph.
 * <ul>
 *     <li>{@code routine.define}: config {@code name}; where the routine starts;</li>
 *     <li>{@code routine.parameter}: config {@code routine}, {@code name} and {@code type}, the selectable type of the
 *     parameter; output {@code value} is the argument the caller passed;</li>
 *     <li>{@code routine.<name>}: calls the routine, one input per parameter, then continues with {@code next}.</li>
 * </ul>
 * Call types are not part of the platform: like macro uses, they are derived from the graph that defines the
 * routine. Each routine becomes a private method of the generated class, so logic several entry points share is
 * written once.
 */
public final class Routines {
    public static final String DEFINE = "routine.define";
    public static final String PARAMETER = "routine.parameter";
    public static final String CALL_PREFIX = "routine.";
    static final String NAME = "name";
    static final String ROUTINE = "routine";
    static final String VALUE = "value";
    static final String TYPE = "type";
    static final String NEXT = "next";
    private static final Pattern VALID_NAME = Pattern.compile("[a-z][a-z0-9_]*");
    private static final Set<String> RESERVED = Set.of("define", "parameter");

    private Routines() {}

    /**
     * A routine of a graph.
     *
     * @param name       its name
     * @param define     id of its {@code routine.define} node
     * @param parameters parameter types by name, in the order the routine's method declares them
     */
    record Routine(String name, String define, Map<String, PortType> parameters) {
        /**
         * @return the name of the generated method
         */
        String method() {
            return javaName(name);
        }
    }

    /**
     * @param types the platform's type mapping; the parameter node offers its selectable types
     * @return {@code routine.define} and the parameter node types
     */
    public static List<NodeDefinition> boundary(TypeMapping types) {
        List<NodeDefinition> definitions = new ArrayList<>();
        definitions.add(new EntryDefinition(
                NodeDescriptor.builder(DEFINE, NodeKind.ENTRY)
                        .category("routine")
                        .name("Define Routine")
                        .description("Starts a routine: a chain any other chain of the same graph can call by name")
                        .config(NAME, String.class, null)
                        .execution(NEXT)
                        .build(),
                "routine",
                List.of(),
                (port, ctx) -> {
                    throw new IllegalArgumentException("routine.define has no outputs");
                }));
        definitions.add(new ExpressionDefinition(
                NodeDescriptor.builder(PARAMETER, NodeKind.VALUE)
                        .category("routine")
                        .name("Routine Parameter")
                        .description("A value the caller of the routine passes in")
                        .config(ROUTINE, String.class, null)
                        .config(NAME, String.class, null)
                        .typeConfig(TYPE, "T", List.copyOf(types.selectable().keySet()))
                        .output(VALUE, PortType.variable("T"))
                        .describe("value", "What the caller passed for this parameter")
                        .build(),
                (port, ctx) -> {
                    throw new IllegalStateException("Routine parameters are resolved by the emitter");
                }));
        return definitions;
    }

    /**
     * @param nodes the graph's nodes
     * @param types the platform's type mapping
     * @return the routine node types of the graph: boundary nodes and one call type per routine
     */
    public static List<NodeDefinition> definitions(Map<String, NodeInstance> nodes, TypeMapping types) {
        List<NodeDefinition> definitions = new ArrayList<>(boundary(types));
        of(nodes, types).values().forEach(routine -> definitions.add(call(routine, types)));
        return definitions;
    }

    /**
     * The routines of a graph. A routine whose name is missing or taken twice is left out; {@link #check} reports it.
     *
     * @param nodes the graph's nodes
     * @param types the platform's type mapping
     * @return routines by name
     */
    static Map<String, Routine> of(Map<String, NodeInstance> nodes, TypeMapping types) {
        Map<String, String> defines = new LinkedHashMap<>();
        Set<String> duplicated = new HashSet<>();
        nodes.forEach((id, node) -> {
            if (!node.type().equals(DEFINE)) return;
            String name = text(node, NAME);
            if (name == null || !validName(name)) return;
            if (defines.putIfAbsent(name, id) != null) duplicated.add(name);
        });
        duplicated.forEach(defines::remove);
        Map<String, Routine> routines = new LinkedHashMap<>();
        defines.forEach(
                (name, define) -> routines.put(name, new Routine(name, define, parameters(nodes, name, types))));
        return routines;
    }

    /**
     * Problems of a graph's routines: bad or taken names, parameters that name no routine or clash in type, calls
     * that loop back into a routine, and routines never called.
     *
     * @param nodes the graph's nodes
     * @param types the platform's type mapping
     * @return the problems found
     */
    public static List<GraphProblem> check(Map<String, NodeInstance> nodes, TypeMapping types) {
        List<GraphProblem> problems = new ArrayList<>();
        Map<String, String> names = new LinkedHashMap<>();
        nodes.forEach((id, node) -> {
            if (!node.type().equals(DEFINE)) return;
            String name = text(node, NAME);
            if (name == null || !validName(name)) {
                problems.add(GraphProblem.error(
                        id,
                        NAME,
                        "Routine names are lower case letters, digits and underscores, not define or parameter"));
                return;
            }
            String other = names.putIfAbsent(name, id);
            if (other != null)
                problems.add(GraphProblem.error(id, NAME, "Routine %s is defined twice".formatted(name)));
        });
        Map<String, Map<String, PortType>> seen = new LinkedHashMap<>();
        Map<String, Set<String>> javaNames = new LinkedHashMap<>();
        nodes.forEach((id, node) -> {
            PortType type = parameterType(node, types);
            if (type == null) return;
            String routine = text(node, ROUTINE);
            String name = text(node, NAME);
            if (routine == null || !names.containsKey(routine)) {
                problems.add(GraphProblem.error(id, ROUTINE, "No routine %s in this graph".formatted(routine)));
                return;
            }
            if (name == null || !validName(name)) {
                problems.add(
                        GraphProblem.error(id, NAME, "Parameter names are lower case letters, digits and underscores"));
                return;
            }
            PortType previous =
                    seen.computeIfAbsent(routine, key -> new LinkedHashMap<>()).putIfAbsent(name, type);
            if (previous != null && !previous.equals(type)) {
                problems.add(GraphProblem.error(
                        id, NAME, "Parameter %s of routine %s has two types".formatted(name, routine)));
            } else if (previous == null
                    && !javaNames
                            .computeIfAbsent(routine, key -> new HashSet<>())
                            .add(javaName(name))) {
                problems.add(GraphProblem.error(
                        id,
                        NAME,
                        "Parameter %s of routine %s clashes with another parameter's name".formatted(name, routine)));
            }
        });
        Map<String, Set<String>> calls = new LinkedHashMap<>();
        names.forEach((name, define) -> calls.put(name, calledFrom(nodes, define)));
        Set<String> called = new HashSet<>();
        nodes.values().forEach(node -> {
            String callee = callee(node.type());
            if (callee != null) called.add(callee);
        });
        names.forEach((name, define) -> {
            if (reaches(calls, name, name)) {
                problems.add(GraphProblem.error(define, null, "Routine %s calls itself".formatted(name)));
            }
            if (!called.contains(name)) {
                problems.add(GraphProblem.warning(define, null, "Routine %s is never called".formatted(name)));
            }
        });
        return problems;
    }

    /**
     * @param type a node type id
     * @return the routine a call type calls, or {@code null} when it is no call
     */
    static @Nullable String callee(String type) {
        if (!type.startsWith(CALL_PREFIX) || type.equals(DEFINE) || type.equals(PARAMETER)) return null;
        return type.substring(CALL_PREFIX.length());
    }

    /**
     * @param name a routine or parameter name
     * @return its name in generated code
     */
    static String javaName(String name) {
        return Identifiers.first(Identifiers.lowerCamel(name), candidate -> true);
    }

    private static StatementDefinition call(Routine routine, TypeMapping types) {
        NodeDescriptor.Builder descriptor = NodeDescriptor.builder(CALL_PREFIX + routine.name(), NodeKind.ACTION)
                .category("routine")
                .name("Call " + routine.name())
                .description("Runs the routine %s, then continues".formatted(routine.name()));
        routine.parameters().forEach(descriptor::input);
        descriptor.execution(NEXT);
        return new StatementDefinition(descriptor.build(), ctx -> CodeBlock.builder()
                .addStatement(
                        "$N($L)",
                        routine.method(),
                        routine.parameters().keySet().stream().map(ctx::input).collect(CodeBlock.joining(", ")))
                .add(ctx.chain(NEXT))
                .build());
    }

    private static Map<String, PortType> parameters(
            Map<String, NodeInstance> nodes, String routine, TypeMapping types) {
        Map<String, PortType> parameters = new LinkedHashMap<>();
        nodes.values().forEach(node -> {
            PortType type = parameterType(node, types);
            String name = text(node, NAME);
            if (type != null && name != null && routine.equals(text(node, ROUTINE))) parameters.putIfAbsent(name, type);
        });
        return parameters;
    }

    private static Set<String> calledFrom(Map<String, NodeInstance> nodes, String define) {
        Set<String> callees = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Deque<String> open = new ArrayDeque<>(List.of(define));
        while (!open.isEmpty()) {
            NodeInstance node = nodes.get(open.pop());
            if (node == null) continue;
            String callee = callee(node.type());
            if (callee != null) callees.add(callee);
            node.executions()
                    .values()
                    .forEach(targets -> targets.forEach(target -> {
                        if (visited.add(target)) open.push(target);
                    }));
        }
        return callees;
    }

    private static boolean reaches(Map<String, Set<String>> calls, String from, String target) {
        Set<String> visited = new HashSet<>();
        Deque<String> open = new ArrayDeque<>(calls.getOrDefault(from, Set.of()));
        while (!open.isEmpty()) {
            String next = open.pop();
            if (next.equals(target)) return true;
            if (visited.add(next)) open.addAll(calls.getOrDefault(next, Set.of()));
        }
        return false;
    }

    private static @Nullable PortType parameterType(NodeInstance node, TypeMapping types) {
        if (!node.type().equals(PARAMETER)) return null;
        String picked = text(node, TYPE);
        return types.selectable()
                .get(
                        picked != null
                                ? picked
                                : types.selectable().keySet().iterator().next());
    }

    private static @Nullable String text(NodeInstance node, String field) {
        JsonNode value = node.config().get(field);
        return value != null && value.isString() ? value.asString() : null;
    }

    private static boolean validName(String name) {
        return VALID_NAME.matcher(name).matches() && !RESERVED.contains(name);
    }
}
