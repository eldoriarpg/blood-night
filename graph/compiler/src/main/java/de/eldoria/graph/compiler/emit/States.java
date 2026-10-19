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
import de.eldoria.graph.model.Edge;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.model.State;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * States: nested modes of a graph. A node tagged with a state only runs its entries while the graph is in that state
 * or in a state inside it; untagged nodes always run.
 * <ul>
 *     <li>{@code state.enter} and {@code state.exit}: entries of the node's state, run when the graph enters or leaves
 *     it;</li>
 *     <li>{@code state.can_enter}: the node's state's enter condition, its input {@code allowed};</li>
 *     <li>{@code state.go_to}, {@code state.go_to_one_of}: move to a state, or to one of up to four picked by
 *     weight;</li>
 *     <li>{@code state.current}, {@code state.in}, {@code state.time}: read where the graph is.</li>
 * </ul>
 * A node may read pure values of its own state, of the states it lies in and of the part that always runs, and only
 * chains nodes of its own state. Routines belong to the part that always runs.
 */
public final class States {
    public static final String ENTER = "state.enter";
    public static final String EXIT = "state.exit";
    public static final String CAN_ENTER = "state.can_enter";
    public static final String GO_TO = "state.go_to";
    public static final String GO_TO_ONE_OF = "state.go_to_one_of";
    public static final String CURRENT = "state.current";
    public static final String IN = "state.in";
    public static final String TIME = "state.time";
    /** How many states {@code state.go_to_one_of} picks from at most. */
    public static final int CHOICES = 4;

    static final String STATE = "state";
    static final String ALLOWED = "allowed";
    static final String NEXT = "next";
    static final String MOVED = "moved";
    private static final Pattern VALID_NAME = Pattern.compile("[a-z][a-z0-9_]*");
    private static final Set<String> SINGLE = Set.of(ENTER, EXIT, CAN_ENTER);

    private States() {}

    /**
     * The states of a graph numbered depth first, so a state and everything inside it form one range.
     *
     * @param names           names by number
     * @param parents         each state's parent number, -1 at the top
     * @param lasts           the highest number inside each state
     * @param initialChildren each state's initial child, -1 for a leaf
     * @param initial         the initial top-level state
     */
    record Layout(List<String> names, int[] parents, int[] lasts, int[] initialChildren, int initial) {
        int number(String name) {
            int number = names.indexOf(name);
            if (number < 0) throw new IllegalArgumentException("No state " + name);
            return number;
        }

        boolean contains(int outer, int inner) {
            return inner >= outer && inner <= lasts[outer];
        }
    }

    /**
     * @param states a graph's states, checked by {@link #check}
     * @return their layout
     */
    static Layout layout(List<State> states) {
        Map<String, List<State>> children = new LinkedHashMap<>();
        states.forEach(state -> children.computeIfAbsent(state.parent(), key -> new ArrayList<>())
                .add(state));
        List<String> names = new ArrayList<>();
        List<Integer> parents = new ArrayList<>();
        Map<String, Integer> lasts = new HashMap<>();
        number(null, -1, children, names, parents, lasts);
        int[] last = new int[names.size()];
        int[] initialChildren = new int[names.size()];
        int initial = -1;
        for (int index = 0; index < names.size(); index++) {
            String name = names.get(index);
            last[index] = lasts.get(name);
            initialChildren[index] = -1;
            for (State child : children.getOrDefault(name, List.of())) {
                if (child.initial()) initialChildren[index] = names.indexOf(child.name());
            }
        }
        for (State top : children.getOrDefault(null, List.of())) {
            if (top.initial()) initial = names.indexOf(top.name());
        }
        return new Layout(
                List.copyOf(names),
                parents.stream().mapToInt(Integer::intValue).toArray(),
                last,
                initialChildren,
                initial);
    }

    private static void number(
            @Nullable String parent,
            int parentNumber,
            Map<String, List<State>> children,
            List<String> names,
            List<Integer> parents,
            Map<String, Integer> lasts) {
        for (State state : children.getOrDefault(parent, List.of())) {
            names.add(state.name());
            parents.add(parentNumber);
            int number = names.size() - 1;
            number(state.name(), number, children, names, parents, lasts);
            lasts.put(state.name(), names.size() - 1);
        }
    }

    /**
     * @return the state node types
     */
    public static List<NodeDefinition> definitions() {
        List<NodeDefinition> definitions = new ArrayList<>();
        definitions.add(boundary(ENTER, "On Enter State", "Runs when the graph enters the state this node lies in"));
        definitions.add(boundary(EXIT, "On Exit State", "Runs when the graph leaves the state this node lies in"));
        definitions.add(new ExpressionDefinition(
                NodeDescriptor.builder(CAN_ENTER, NodeKind.PURE)
                        .category("state")
                        .name("Can Enter State")
                        .description("Whether the graph may enter the state this node lies in; without one it always"
                                + " may")
                        .input(ALLOWED, boolean.class)
                        .output(ALLOWED, boolean.class)
                        .describe(ALLOWED, "True: the state may be entered")
                        .build(),
                (port, ctx) -> ctx.input(ALLOWED)));
        definitions.add(new StatementDefinition(
                NodeDescriptor.builder(GO_TO, NodeKind.ACTION)
                        .category("state")
                        .name("Go To State")
                        .description("Leaves the current state and enters another; nothing happens when the graph is"
                                + " in it already or the state says no")
                        .config(STATE, String.class, null)
                        .output(MOVED, boolean.class)
                        .execution(NEXT)
                        .describe(MOVED, "True: the graph moved")
                        .build(),
                ctx -> CodeBlock.builder()
                        .addStatement(
                                "final boolean $L = $L.goTo($L)",
                                ctx.bind(MOVED, NEXT),
                                ctx.stateMachine(),
                                ctx.stateNumber(ctx.configValue(STATE).asString()))
                        .add(ctx.chain(NEXT))
                        .build()));
        definitions.add(goToOneOf());
        definitions.add(new ExpressionDefinition(
                NodeDescriptor.builder(CURRENT, NodeKind.VALUE)
                        .category("state")
                        .name("Current State")
                        .description("The name of the innermost state the graph is in")
                        .output("name", String.class)
                        .build(),
                (port, ctx) -> CodeBlock.of("$L.current()", ctx.stateMachine())));
        definitions.add(new ExpressionDefinition(
                NodeDescriptor.builder(IN, NodeKind.VALUE)
                        .category("state")
                        .name("In State")
                        .description("Whether the graph is in a state or in a state inside it")
                        .config(STATE, String.class, null)
                        .output("result", boolean.class)
                        .build(),
                (port, ctx) -> CodeBlock.of(
                        "$L.in($L)",
                        ctx.stateMachine(),
                        ctx.stateNumber(ctx.configValue(STATE).asString()))));
        definitions.add(new ExpressionDefinition(
                NodeDescriptor.builder(TIME, NodeKind.VALUE)
                        .category("state")
                        .name("Time In State")
                        .description("Seconds since the graph last moved to another state")
                        .output("seconds", double.class)
                        .build(),
                (port, ctx) -> CodeBlock.of("$L.seconds()", ctx.stateMachine())));
        return definitions;
    }

    private static EntryDefinition boundary(String id, String name, String description) {
        return new EntryDefinition(
                NodeDescriptor.builder(id, NodeKind.ENTRY)
                        .category("state")
                        .name(name)
                        .description(description)
                        .execution(NEXT)
                        .build(),
                STATE,
                List.of(),
                (port, ctx) -> {
                    throw new IllegalArgumentException(id + " has no outputs");
                });
    }

    private static StatementDefinition goToOneOf() {
        NodeDescriptor.Builder descriptor = NodeDescriptor.builder(GO_TO_ONE_OF, NodeKind.ACTION)
                .category("state")
                .name("Go To One Of")
                .description("Goes to one of up to four states, picked by weight among those that can be entered");
        for (int choice = 1; choice <= CHOICES; choice++) {
            descriptor.config(choiceState(choice), String.class, StringNode.valueOf(""));
            descriptor.optionalInput(weight(choice), double.class);
            descriptor.describe(
                    weight(choice), "How likely state %d is; 1 when not given, 0 leaves it out".formatted(choice));
        }
        descriptor.output(MOVED, boolean.class).output(STATE, String.class).execution(NEXT);
        descriptor.describe(MOVED, "True: the graph moved").describe(STATE, "The state it moved to; empty if none");
        return new StatementDefinition(descriptor.build(), ctx -> {
            List<CodeBlock> targets = new ArrayList<>();
            List<CodeBlock> weights = new ArrayList<>();
            for (int choice = 1; choice <= CHOICES; choice++) {
                JsonNode picked = ctx.configValue(choiceState(choice));
                if (!picked.isString() || picked.asString().isBlank()) continue;
                targets.add(CodeBlock.of("$L", ctx.stateNumber(picked.asString())));
                weights.add(ctx.connected(weight(choice)) ? ctx.input(weight(choice)) : CodeBlock.of("1.0"));
            }
            CodeBlock machine = ctx.stateMachine();
            CodeBlock moved = ctx.bind(MOVED, NEXT);
            CodeBlock state = ctx.bind(STATE, NEXT);
            return CodeBlock.builder()
                    .addStatement(
                            "final int $L = $L.goToOneOf(new int[] {$L}, new double[] {$L})",
                            CodeBlock.of("$LTarget", state),
                            machine,
                            CodeBlock.join(targets, ", "),
                            CodeBlock.join(weights, ", "))
                    .addStatement("final boolean $L = $LTarget >= 0", moved, state)
                    .addStatement("final String $L = $L.name($LTarget)", state, machine, state)
                    .add(ctx.chain(NEXT))
                    .build();
        });
    }

    static String choiceState(int choice) {
        return "state_" + choice;
    }

    static String weight(int choice) {
        return "weight_" + choice;
    }

    /**
     * Problems of a graph's states: bad or taken names, unknown parents, a missing or second initial state, nodes in
     * unknown states, a second enter, exit or enter condition in one state, state nodes naming unknown states, and
     * wiring across states.
     *
     * @param graph the graph
     * @return the problems found
     */
    public static List<GraphProblem> check(Graph graph) {
        List<GraphProblem> problems = new ArrayList<>();
        Map<String, State> states = new LinkedHashMap<>();
        for (State state : graph.states()) {
            if (!VALID_NAME.matcher(state.name()).matches()) {
                problems.add(GraphProblem.error(
                        null, null, "State names are lower case letters, digits and underscores: " + state.name()));
            } else if (states.putIfAbsent(state.name(), state) != null) {
                problems.add(GraphProblem.error(null, null, "State %s exists twice".formatted(state.name())));
            } else if (state.parent() != null && !states.containsKey(state.parent())) {
                problems.add(GraphProblem.error(
                        null,
                        null,
                        "State %s lies in %s, which is not a state listed before it"
                                .formatted(state.name(), state.parent())));
            }
        }
        if (!states.isEmpty()) {
            Map<String, Integer> initials = new HashMap<>();
            Set<String> parents = new HashSet<>();
            states.values().forEach(state -> {
                String parent = state.parent() == null ? "" : state.parent();
                parents.add(parent);
                if (state.initial()) initials.merge(parent, 1, Integer::sum);
            });
            parents.forEach(parent -> {
                int count = initials.getOrDefault(parent, 0);
                String where = parent.isEmpty() ? "the top level" : "state " + parent;
                if (count != 1) {
                    problems.add(GraphProblem.error(
                            null, null, "%s needs exactly one initial state, has %d".formatted(where, count)));
                }
            });
        }
        Map<String, Set<String>> single = new HashMap<>();
        graph.nodes().forEach((id, node) -> {
            String state = node.state();
            if (state != null && !states.containsKey(state)) {
                problems.add(GraphProblem.error(id, null, "No state %s in this graph".formatted(state)));
                return;
            }
            if (SINGLE.contains(node.type())) {
                if (state == null) {
                    problems.add(GraphProblem.error(id, null, "This node only works inside a state"));
                } else if (!single.computeIfAbsent(node.type(), key -> new HashSet<>())
                        .add(state)) {
                    problems.add(GraphProblem.error(id, null, "State %s has this node twice".formatted(state)));
                }
            }
            if (node.type().equals(Routines.DEFINE) && state != null) {
                problems.add(GraphProblem.error(id, null, "Routines belong to the part that always runs"));
            }
            if (node.type().equals(GO_TO) || node.type().equals(IN)) named(id, node, STATE, states.keySet(), problems);
            if (node.type().equals(GO_TO_ONE_OF)) {
                boolean any = false;
                for (int choice = 1; choice <= CHOICES; choice++) {
                    any |= named(id, node, choiceState(choice), states.keySet(), problems);
                }
                if (!any) problems.add(GraphProblem.error(id, null, "Pick at least one state"));
            }
            node.inputs().forEach((port, edge) -> wiring(graph, id, port, edge, states, problems));
            node.executions()
                    .values()
                    .forEach(targets -> targets.forEach(target -> {
                        NodeInstance chained = graph.nodes().get(target);
                        if (chained != null && !same(state, chained.state())) {
                            problems.add(GraphProblem.error(target, null, "Chained from a node of another state"));
                        }
                    }));
        });
        return problems;
    }

    /**
     * @return whether a config field names a state, reporting a name that is no state
     */
    private static boolean named(
            String id, NodeInstance node, String field, Set<String> states, List<GraphProblem> problems) {
        JsonNode value = node.config().get(field);
        String name = value != null && value.isString() ? value.asString() : null;
        if (name == null || name.isBlank()) return false;
        if (!states.contains(name)) {
            problems.add(GraphProblem.error(id, field, "No state %s in this graph".formatted(name)));
            return false;
        }
        return true;
    }

    private static void wiring(
            Graph graph,
            String reader,
            String port,
            Edge edge,
            Map<String, State> states,
            List<GraphProblem> problems) {
        NodeInstance source = graph.nodes().get(edge.node());
        NodeInstance target = graph.nodes().get(reader);
        if (source == null || target == null || source.state() == null) return;
        for (String state = target.state(); state != null; state = parentOf(states, state)) {
            if (state.equals(source.state())) return;
        }
        problems.add(GraphProblem.error(
                reader, port, "Reads a value of state %s, which this node does not lie in".formatted(source.state())));
    }

    private static @Nullable String parentOf(Map<String, State> states, String state) {
        State found = states.get(state);
        return found == null ? null : found.parent();
    }

    private static boolean same(@Nullable String first, @Nullable String second) {
        return first == null ? second == null : first.equals(second);
    }
}
