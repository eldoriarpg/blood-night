/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler.macro;

import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.definition.MacroDefinition;
import de.eldoria.graph.model.Edge;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Replaces every macro use in a graph with the macro's body.
 * <p>
 * Body nodes are copied under {@code <use>/<node>}. Edges from input boundary nodes become the edges connected to the
 * use; edges reading the use's outputs become the edges feeding the matching output boundary node; chaining the use
 * chains what follows the start node, and an end node continues with what was chained to that port of the use.
 * Nested macros expand in further rounds.
 * <p>
 * Every copied node records where it came from, so problems found later can be reported on the node the user placed
 * and traced into the macro.
 */
public final class MacroExpander {
    private static final int MAX_DEPTH = 16;

    private MacroExpander() {}

    /**
     * Where an expanded node came from.
     *
     * @param node  the node in the graph as stored, i.e. the outermost macro use
     * @param trace path into the macros
     */
    record Origin(String node, List<MacroFrame> trace) {}

    /**
     * A graph without macro uses, and the origin of every node that came out of a macro.
     *
     * @param graph   the expanded graph
     * @param origins origin of every expanded node
     * @param inputs  macro input name of every pass-through node; problems on its {@code value} port belong to that
     *                input of the macro use
     */
    public record Expansion(Graph graph, Map<String, Origin> origins, Map<String, String> inputs) {
        public GraphProblem locate(GraphProblem problem) {
            if (problem.node() == null) return problem;
            Origin origin = origins.get(problem.node());
            if (origin == null) return problem;
            List<MacroFrame> trace = new ArrayList<>(origin.trace());
            trace.addAll(problem.trace());
            GraphProblem located = problem.at(origin.node(), trace);
            String input = inputs.get(problem.node());
            if (input != null && MacroBoundary.VALUE.equals(problem.port())) {
                located =
                        new GraphProblem(located.severity(), located.node(), input, located.message(), located.trace());
            }
            return located;
        }

        public List<GraphProblem> locate(List<GraphProblem> problems) {
            return problems.stream().map(this::locate).toList();
        }
    }

    /**
     * @param graph  a graph possibly using macros
     * @param macros macro definitions by node type
     * @return the expanded graph
     * @throws GraphCompilationException when a use is wired in a way the macro does not support
     */
    public static Expansion expand(Graph graph, Map<String, MacroDefinition> macros) throws GraphCompilationException {
        Map<String, NodeInstance> nodes = new TreeMap<>(graph.nodes());
        Map<String, Origin> origins = new HashMap<>();
        Map<String, String> inputs = new HashMap<>();
        Set<GraphProblem> problems = new LinkedHashSet<>();
        for (int depth = 0; ; depth++) {
            List<String> uses = nodes.entrySet().stream()
                    .filter(entry -> macros.containsKey(entry.getValue().type()))
                    .map(Map.Entry::getKey)
                    .toList();
            if (uses.isEmpty()) break;
            if (depth == MAX_DEPTH) {
                uses.forEach(use ->
                        problems.add(GraphProblem.error(use, null, "Macros are nested deeper than " + MAX_DEPTH)));
                break;
            }
            for (String use : uses)
                new Splice(use, macros.get(nodes.get(use).type()), nodes, origins, inputs, problems).run();
        }
        Expansion expansion =
                new Expansion(new Graph(nodes, Map.of(), graph.states()), Map.copyOf(origins), Map.copyOf(inputs));
        if (!problems.isEmpty()) throw new GraphCompilationException(expansion.locate(List.copyOf(problems)), null);
        return expansion;
    }

    private static final class Splice {
        private final String useId;
        private final NodeInstance use;
        private final MacroDefinition definition;
        private final Map<String, NodeInstance> nodes;
        private final Map<String, Origin> origins;
        private final Map<String, String> inputs;
        private final Set<GraphProblem> problems;
        private final String prefix;
        private final Map<String, String> endNames = new HashMap<>();
        private final Graph body;

        Splice(
                String useId,
                MacroDefinition definition,
                Map<String, NodeInstance> nodes,
                Map<String, Origin> origins,
                Map<String, String> inputs,
                Set<GraphProblem> problems) {
            this.useId = useId;
            this.use = nodes.get(useId);
            this.definition = definition;
            this.nodes = nodes;
            this.origins = origins;
            this.inputs = inputs;
            this.problems = problems;
            this.prefix = useId + "/";
            this.body = definition.macro().body();
            definition.endNodes().forEach((port, node) -> endNames.put(node, port));
        }

        void run() {
            checkUse();
            nodes.remove(useId);
            Origin useOrigin = origins.getOrDefault(useId, new Origin(useId, List.of()));
            definition.inputNodes().forEach((name, node) -> passThrough(name, node, useOrigin));
            body.nodes().forEach((id, node) -> {
                if (MacroBoundary.isBoundary(node.type())) return;
                Map<String, Edge> inputs = new LinkedHashMap<>();
                node.inputs().forEach((port, edge) -> inner(edge).ifPresent(resolved -> inputs.put(port, resolved)));
                Map<String, List<String>> executions = new LinkedHashMap<>();
                node.executions().forEach((port, targets) -> executions.put(port, chain(targets)));
                nodes.put(
                        prefix + id,
                        new NodeInstance(
                                node.type(),
                                node.config(),
                                inputs,
                                node.literals(),
                                executions,
                                use.state(),
                                node.editor()));
                List<MacroFrame> trace = new ArrayList<>(useOrigin.trace());
                trace.add(new MacroFrame(definition.macro().id(), id));
                origins.put(prefix + id, new Origin(useOrigin.node(), trace));
            });
            rewireCallers();
        }

        /**
         * Puts a typed node where the input boundary node was, fed by what the user connected to the macro input.
         */
        private void passThrough(String name, String node, Origin useOrigin) {
            Edge connected = use.inputs().get(name);
            JsonNode literal = use.literals().get(name);
            if (connected == null && literal == null) {
                problems.add(GraphProblem.error(useId, name, "Macro input is not connected"));
                return;
            }
            String type = definition.inputTypes().get(name);
            Map<String, Edge> edges = connected == null ? Map.of() : Map.of(MacroBoundary.VALUE, connected);
            Map<String, JsonNode> literals = literal == null ? Map.of() : Map.of(MacroBoundary.VALUE, literal);
            nodes.put(
                    prefix + node,
                    new NodeInstance(
                            MacroBoundary.PASS_PREFIX + type, Map.of(), edges, literals, Map.of(), use.state(), null));
            origins.put(prefix + node, useOrigin);
            inputs.put(prefix + node, name);
        }

        private void checkUse() {
            Stream.concat(use.inputs().keySet().stream(), use.literals().keySet().stream())
                    .filter(port -> !definition.inputNodes().containsKey(port))
                    .forEach(port -> problems.add(GraphProblem.error(
                            useId,
                            port,
                            "Macro %s has no input %s"
                                    .formatted(definition.macro().id(), port))));
            use.executions().keySet().stream()
                    .filter(port -> !definition.endNodes().containsKey(port))
                    .forEach(port -> problems.add(GraphProblem.error(
                            useId,
                            port,
                            "Macro %s has no exit %s"
                                    .formatted(definition.macro().id(), port))));
            use.config()
                    .keySet()
                    .forEach(field -> problems.add(GraphProblem.error(useId, field, "Macros have no config")));
        }

        private Optional<Edge> inner(Edge edge) {
            return Optional.of(Edge.to(prefix + edge.node(), edge.port()));
        }

        private List<String> chain(List<String> targets) {
            List<String> result = new ArrayList<>();
            for (String target : targets) {
                String exit = endNames.get(target);
                if (exit != null) result.addAll(use.executions().getOrDefault(exit, List.of()));
                else result.add(prefix + target);
            }
            return result;
        }

        private void rewireCallers() {
            for (Map.Entry<String, NodeInstance> entry : List.copyOf(nodes.entrySet())) {
                String id = entry.getKey();
                NodeInstance node = entry.getValue();
                boolean changed = false;
                Map<String, Edge> inputs = new LinkedHashMap<>(node.inputs());
                for (Map.Entry<String, Edge> input : node.inputs().entrySet()) {
                    if (!input.getValue().node().equals(useId)) continue;
                    changed = true;
                    inputs.remove(input.getKey());
                    output(id, input.getKey(), input.getValue().port())
                            .ifPresent(edge -> inputs.put(input.getKey(), edge));
                }
                Map<String, List<String>> executions = new LinkedHashMap<>(node.executions());
                for (Map.Entry<String, List<String>> execution :
                        node.executions().entrySet()) {
                    if (!execution.getValue().contains(useId)) continue;
                    changed = true;
                    List<String> replaced = new ArrayList<>();
                    for (String target : execution.getValue()) {
                        if (!target.equals(useId)) replaced.add(target);
                        else replaced.addAll(entered(id, execution.getKey()));
                    }
                    executions.put(execution.getKey(), replaced);
                }
                if (changed)
                    nodes.put(
                            id,
                            new NodeInstance(
                                    node.type(),
                                    node.config(),
                                    inputs,
                                    node.literals(),
                                    executions,
                                    node.state(),
                                    node.editor()));
            }
        }

        private Optional<Edge> output(String reader, String readerPort, String port) {
            String outputNode = definition.outputNodes().get(port);
            if (outputNode == null) {
                problems.add(GraphProblem.error(
                        reader,
                        readerPort,
                        "Macro %s has no output %s".formatted(definition.macro().id(), port)));
                return Optional.empty();
            }
            Edge value = body.nodes().get(outputNode).inputs().get(MacroBoundary.VALUE);
            return value == null ? Optional.empty() : inner(value);
        }

        private List<String> entered(String chainer, String port) {
            if (definition.startNode() == null) {
                problems.add(GraphProblem.error(
                        chainer,
                        port,
                        "Macro %s has no start and cannot be chained"
                                .formatted(definition.macro().id())));
                return List.of();
            }
            return chain(
                    body.nodes().get(definition.startNode()).executions().getOrDefault(MacroBoundary.NEXT, List.of()));
        }
    }
}
