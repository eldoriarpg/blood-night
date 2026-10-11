package de.eldoria.graph.compiler;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ConfigField;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.Port;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.model.Edge;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Checks a graph against a platform before any code is emitted.
 * <p>
 * Structure: node types exist, config values are valid, inputs point at existing outputs, required inputs are
 * connected, execution ports chain statements only and each statement at most once, neither execution nor data edges
 * form cycles.
 * <p>
 * Types: type variables are inferred per node from its connected inputs, then every edge is checked for
 * assignability. Inference follows data edges, so nodes are resolved upstream first.
 * <p>
 * Whether a value is readable where it is read (entry outputs inside their own chain, loop variables inside the loop)
 * depends on where code is placed and is checked by the emitter.
 */
final class GraphValidator {
    private final GraphPlatform platform;

    GraphValidator(GraphPlatform platform) {
        this.platform = platform;
    }

    /**
     * @param graph the graph
     * @return the checked graph
     * @throws GraphCompilationException when any error is found; carries warnings too
     */
    ValidatedGraph validate(Graph graph) throws GraphCompilationException {
        return new Run(graph).run();
    }

    private final class Run {
        private final Graph graph;
        private final List<GraphProblem> problems = new ArrayList<>();
        private final Map<String, NodeDefinition> definitions = new LinkedHashMap<>();
        private final Map<String, Map<String, CodeBlock>> config = new HashMap<>();
        private final Map<String, Map<String, PortType>> inputTypes = new HashMap<>();
        private final Map<String, Map<String, PortType>> outputTypes = new HashMap<>();
        private final Set<String> resolving = new HashSet<>();

        Run(Graph graph) {
            this.graph = graph;
        }

        ValidatedGraph run() throws GraphCompilationException {
            graph.nodes().forEach((id, node) -> platform.node(node.type()).ifPresentOrElse(
                    definition -> definitions.put(id, definition),
                    () -> problems.add(GraphProblem.error(id, null, "Unknown node type " + node.type()))));
            definitions.forEach(this::checkConfig);
            definitions.forEach(this::checkInputs);
            Map<String, String> parents = checkExecutions();
            checkReachability(parents);
            definitions.keySet().forEach(this::resolveTypes);

            if (problems.stream().anyMatch(problem -> problem.severity() == GraphProblem.Severity.ERROR)) {
                throw new GraphCompilationException(problems, null);
            }
            Map<String, ValidatedGraph.Node> nodes = new LinkedHashMap<>();
            definitions.forEach((id, definition) -> nodes.put(id, new ValidatedGraph.Node(id, graph.nodes().get(id), definition,
                    Map.copyOf(inputTypes.get(id)), Map.copyOf(outputTypes.get(id)), Map.copyOf(config.get(id)))));
            List<String> entries = definitions.entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof EntryDefinition)
                    .map(Map.Entry::getKey)
                    .toList();
            return new ValidatedGraph(platform, nodes, entries, List.copyOf(problems));
        }

        private void checkConfig(String id, NodeDefinition definition) {
            NodeInstance instance = graph.nodes().get(id);
            NodeDescriptor descriptor = definition.descriptor();
            Map<String, CodeBlock> literals = new HashMap<>();
            instance.config().keySet().stream()
                    .filter(field -> descriptor.configField(field).isEmpty())
                    .forEach(field -> problems.add(GraphProblem.error(id, field, "Unknown config field")));
            for (ConfigField field : descriptor.config()) {
                JsonNode value = instance.config().getOrDefault(field.id(), field.defaultValue());
                if (value == null) {
                    problems.add(GraphProblem.error(id, field.id(), "Required config value is missing"));
                    continue;
                }
                try {
                    literals.put(field.id(), platform.types().literal(field.type(), value));
                } catch (IllegalArgumentException e) {
                    problems.add(GraphProblem.error(id, field.id(), e.getMessage()));
                }
            }
            config.put(id, literals);
        }

        private void checkInputs(String id, NodeDefinition definition) {
            NodeInstance instance = graph.nodes().get(id);
            NodeDescriptor descriptor = definition.descriptor();
            instance.inputs().forEach((port, edge) -> {
                if (descriptor.input(port).isEmpty()) {
                    problems.add(GraphProblem.error(id, port, "Unknown input"));
                    return;
                }
                NodeDefinition source = definitions.get(edge.node());
                if (source == null) {
                    if (!graph.nodes().containsKey(edge.node())) problems.add(GraphProblem.error(id, port, "Connected to missing node " + edge.node()));
                    return;
                }
                if (source.descriptor().output(edge.port()).isEmpty()) {
                    problems.add(GraphProblem.error(id, port, "Node %s has no output %s".formatted(edge.node(), edge.port())));
                }
            });
            descriptor.inputs().stream()
                    .filter(port -> !port.optional() && !instance.inputs().containsKey(port.id()))
                    .forEach(port -> problems.add(GraphProblem.error(id, port.id(), "Required input is not connected")));
        }

        /**
         * @return the chaining node of every statement that is chained somewhere
         */
        private Map<String, String> checkExecutions() {
            Map<String, String> parents = new HashMap<>();
            definitions.forEach((id, definition) -> graph.nodes().get(id).executions().forEach((port, targets) -> {
                if (definition.descriptor().execution(port).isEmpty()) {
                    problems.add(GraphProblem.error(id, port, "Unknown execution port"));
                    return;
                }
                for (String target : targets) {
                    NodeDefinition targetDefinition = definitions.get(target);
                    if (targetDefinition == null) {
                        if (!graph.nodes().containsKey(target)) problems.add(GraphProblem.error(id, port, "Chains missing node " + target));
                        continue;
                    }
                    if (!(targetDefinition instanceof StatementDefinition)) {
                        problems.add(GraphProblem.error(id, port, "Node %s cannot be chained, it is not an action".formatted(target)));
                        continue;
                    }
                    String previous = parents.putIfAbsent(target, id);
                    if (previous != null) {
                        problems.add(GraphProblem.error(target, null, "Chained more than once, by %s and %s".formatted(previous, id)));
                    }
                }
            }));
            return parents;
        }

        private void checkReachability(Map<String, String> parents) {
            definitions.forEach((id, definition) -> {
                if (!(definition instanceof StatementDefinition)) return;
                Set<String> seen = new HashSet<>();
                String current = id;
                while (parents.containsKey(current)) {
                    if (!seen.add(current)) {
                        problems.add(GraphProblem.error(id, null, "Execution chain loops back onto itself"));
                        return;
                    }
                    current = parents.get(current);
                }
                if (!(definitions.get(current) instanceof EntryDefinition)) {
                    problems.add(GraphProblem.warning(id, null, "Not reachable from any entry point, will not run"));
                }
            });
        }

        private void resolveTypes(String id) {
            if (outputTypes.containsKey(id)) return;
            if (!resolving.add(id)) return;
            NodeDefinition definition = definitions.get(id);
            NodeInstance instance = graph.nodes().get(id);
            Map<String, PortType> bindings = new HashMap<>();
            Map<String, PortType> sources = new LinkedHashMap<>();

            for (Port port : definition.descriptor().inputs()) {
                Edge edge = instance.inputs().get(port.id());
                if (edge == null || !definitions.containsKey(edge.node())) continue;
                if (resolving.contains(edge.node())) {
                    problems.add(GraphProblem.error(id, port.id(), "Data edges form a cycle through node " + edge.node()));
                    continue;
                }
                resolveTypes(edge.node());
                PortType source = outputTypes.get(edge.node()).get(edge.port());
                if (source == null) continue;
                if (source.hasVariables()) {
                    problems.add(GraphProblem.error(id, port.id(), "Type of %s.%s cannot be inferred".formatted(edge.node(), edge.port())));
                    continue;
                }
                sources.put(port.id(), source);
                if (!TypeInference.unify(port.type(), source, bindings)) {
                    problems.add(GraphProblem.error(id, port.id(), "Conflicting types for %s: %s".formatted(port.type().id(), source.id())));
                }
            }

            Map<String, PortType> inputs = new HashMap<>();
            for (Port port : definition.descriptor().inputs()) {
                PortType expected = port.type().resolve(bindings);
                inputs.put(port.id(), expected);
                PortType source = sources.get(port.id());
                if (source != null && !expected.accepts(source)) {
                    problems.add(GraphProblem.error(id, port.id(), "Expected %s but %s provides %s".formatted(
                            platform.types().name(expected), instance.inputs().get(port.id()).node(), platform.types().name(source))));
                }
            }
            Map<String, PortType> outputs = new HashMap<>();
            definition.descriptor().outputs().forEach(port -> outputs.put(port.id(), port.type().resolve(bindings)));
            inputTypes.put(id, inputs);
            outputTypes.put(id, outputs);
            resolving.remove(id);
        }
    }

    /**
     * Infers type variables of a node from the types connected to its inputs.
     */
    static final class TypeInference {
        private TypeInference() {
        }

        /**
         * Binds the variables in {@code expected} so that it matches {@code actual}. Variables bind to boxed types,
         * because they end up as type arguments.
         *
         * @return false when a variable would need two different types
         */
        static boolean unify(PortType expected, PortType actual, Map<String, PortType> bindings) {
            return switch (expected) {
                case PortType.Variable variable -> {
                    PortType boxed = actual.boxed();
                    PortType bound = bindings.putIfAbsent(variable.name(), boxed);
                    yield bound == null || bound.equals(boxed);
                }
                case PortType.Concrete concrete -> {
                    if (!concrete.hasVariables() || !(actual instanceof PortType.Concrete source)) yield true;
                    Optional<PortType.Concrete> lifted = source.asSupertype(concrete.raw());
                    if (lifted.isEmpty() || lifted.get().arguments().size() != concrete.arguments().size()) yield true;
                    boolean consistent = true;
                    for (int i = 0; i < concrete.arguments().size(); i++) {
                        consistent &= unify(concrete.arguments().get(i), lifted.get().arguments().get(i), bindings);
                    }
                    yield consistent;
                }
            };
        }
    }
}
