package de.eldoria.graph.compiler;

import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.MacroDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.model.Macro;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.platform.PlatformException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * The macros users defined, checked against a platform and turned into node definitions.
 * <p>
 * Macros are user data, so a broken macro never breaks the platform: it is left out and its problems are kept per
 * macro, located inside its body. Macros may use other macros; they are checked in dependency order, cycles and uses
 * of broken macros are problems of their own.
 * <p>
 * Each body is checked on its own, with the platform's nodes except entry points plus the boundary nodes. That fixes
 * the macro's ports: inputs from {@code macro.input.*}, outputs typed by what feeds each {@code macro.output}, exits
 * from {@code macro.end}.
 */
public final class MacroLibrary {
    private static final String TYPE_PREFIX = "macro.";
    private static final Set<String> RESERVED = Set.of("input", "output", "start", "end");

    private final Map<String, MacroDefinition> definitions;
    private final Map<String, List<GraphProblem>> problems;

    private MacroLibrary(Map<String, MacroDefinition> definitions, Map<String, List<GraphProblem>> problems) {
        this.definitions = Map.copyOf(definitions);
        this.problems = Map.copyOf(problems);
    }

    /**
     * @return a library without macros
     */
    public static MacroLibrary empty() {
        return new MacroLibrary(Map.of(), Map.of());
    }

    /**
     * @param platform the platform macros are used with
     * @param macros   the macros
     * @return the library; check {@link #problems()} for macros that were left out
     */
    public static MacroLibrary compile(GraphPlatform platform, Collection<Macro> macros) {
        return new Build(platform, macros).run();
    }

    /**
     * @return node definition of every valid macro, by node type ({@code macro.<id>})
     */
    public Map<String, MacroDefinition> definitions() {
        return definitions;
    }

    /**
     * @return problems by macro id; macros with errors are not in {@link #definitions()}
     */
    public Map<String, List<GraphProblem>> problems() {
        return problems;
    }

    private static final class Build {
        private final GraphPlatform platform;
        private final Map<String, Macro> macros = new TreeMap<>();
        private final Map<String, MacroDefinition> definitions = new LinkedHashMap<>();
        private final Map<String, List<GraphProblem>> problems = new TreeMap<>();
        private final List<NodeDefinition> analysisNodes = new ArrayList<>();

        Build(GraphPlatform platform, Collection<Macro> macros) {
            this.platform = platform;
            for (Macro macro : macros) {
                if (!macro.id().matches("[a-z][a-z0-9_]*") || RESERVED.contains(macro.id())) {
                    problem(macro.id(), GraphProblem.error(null, null, "Invalid macro id '%s'".formatted(macro.id())));
                } else if (this.macros.putIfAbsent(macro.id(), macro) != null) {
                    problem(macro.id(), GraphProblem.error(null, null, "Duplicate macro id"));
                }
            }
            platform.nodes().values().stream().filter(node -> !(node instanceof EntryDefinition)).forEach(analysisNodes::add);
            analysisNodes.addAll(MacroBoundary.definitions(platform.types()));
        }

        MacroLibrary run() {
            Set<String> done = new HashSet<>();
            for (String id : macros.keySet()) visit(id, new ArrayList<>(), done);
            return new MacroLibrary(definitions, problems);
        }

        private void visit(String id, List<String> path, Set<String> done) {
            if (done.contains(id)) return;
            if (path.contains(id)) {
                List<String> cycle = new ArrayList<>(path.subList(path.indexOf(id), path.size()));
                cycle.add(id);
                for (String member : cycle.subList(0, cycle.size() - 1)) {
                    problem(member, GraphProblem.error(null, null, "Macros use each other in a cycle: " + String.join(" > ", cycle)));
                    done.add(member);
                }
                return;
            }
            path.add(id);
            Set<String> dependencies = dependencies(macros.get(id));
            for (String dependency : dependencies) visit(dependency, path, done);
            path.removeLast();
            if (done.contains(id)) return;
            done.add(id);
            for (String dependency : dependencies) {
                if (!definitions.containsKey(TYPE_PREFIX + dependency)) {
                    problem(id, GraphProblem.error(null, null, "Uses macro %s, which has errors".formatted(dependency)));
                    return;
                }
            }
            analyse(macros.get(id));
        }

        private Set<String> dependencies(Macro macro) {
            Set<String> result = new HashSet<>();
            for (NodeInstance node : macro.body().nodes().values()) {
                if (!node.type().startsWith(TYPE_PREFIX) || MacroBoundary.isBoundary(node.type())) continue;
                String id = node.type().substring(TYPE_PREFIX.length());
                if (macros.containsKey(id)) result.add(id);
            }
            return result;
        }

        private void analyse(Macro macro) {
            List<GraphProblem> found = new ArrayList<>();
            Map<String, String> inputNodes = new TreeMap<>();
            Map<String, PortType> inputTypes = new TreeMap<>();
            Map<String, String> outputNodes = new TreeMap<>();
            Map<String, String> endNodes = new TreeMap<>();
            List<String> starts = new ArrayList<>();
            macro.body().nodes().forEach((nodeId, node) -> {
                String type = node.type();
                if (type.startsWith(MacroBoundary.INPUT_PREFIX)) {
                    name(nodeId, node, found).ifPresent(name -> {
                        register(inputNodes, name, nodeId, found);
                        PortType portType = platform.types().selectable().get(type.substring(MacroBoundary.INPUT_PREFIX.length()));
                        if (portType != null) inputTypes.put(name, portType);
                    });
                } else if (type.equals(MacroBoundary.OUTPUT)) {
                    name(nodeId, node, found).ifPresent(name -> register(outputNodes, name, nodeId, found));
                } else if (type.equals(MacroBoundary.END)) {
                    name(nodeId, node, found).ifPresent(name -> register(endNodes, name, nodeId, found));
                } else if (type.equals(MacroBoundary.START)) {
                    starts.add(nodeId);
                }
            });
            if (starts.size() > 1) starts.forEach(start -> found.add(GraphProblem.error(start, null, "A macro has at most one start")));
            if (starts.isEmpty() && outputNodes.isEmpty()) found.add(GraphProblem.error(null, null, "A macro needs a start or at least one output"));
            if (starts.isEmpty() && !endNodes.isEmpty()) endNodes.values().forEach(end -> found.add(GraphProblem.error(end, null, "Exits need a start")));

            ValidatedGraph validated = null;
            try {
                MacroExpander.Expansion expansion = MacroExpander.expand(macro.body(), definitions);
                try {
                    validated = new GraphValidator(analysisPlatform(), MacroBoundary.passDefinitions(platform.types())).validate(expansion.graph());
                    found.addAll(expansion.locate(validated.warnings()));
                } catch (GraphCompilationException e) {
                    found.addAll(expansion.locate(e.problems()));
                }
            } catch (GraphCompilationException e) {
                found.addAll(e.problems());
            }

            if (validated == null || found.stream().anyMatch(problem -> problem.severity() == GraphProblem.Severity.ERROR)) {
                problems.computeIfAbsent(macro.id(), key -> new ArrayList<>()).addAll(found);
                return;
            }
            if (!found.isEmpty()) problems.computeIfAbsent(macro.id(), key -> new ArrayList<>()).addAll(found);

            String start = starts.isEmpty() ? null : starts.getFirst();
            NodeDescriptor.Builder descriptor = NodeDescriptor.builder(TYPE_PREFIX + macro.id(), start == null ? NodeKind.PURE : NodeKind.ACTION)
                    .category(macro.category())
                    .name(macro.name())
                    .description(macro.description());
            inputTypes.forEach(descriptor::input);
            ValidatedGraph checked = validated;
            outputNodes.forEach((name, node) -> descriptor.output(name, checked.nodes().get(node).sourceTypes().get(MacroBoundary.VALUE)));
            endNodes.keySet().forEach(descriptor::execution);
            definitions.put(TYPE_PREFIX + macro.id(), new MacroDefinition(descriptor.build(), macro, inputNodes, outputNodes, start, endNodes));
        }

        private GraphPlatform analysisPlatform() {
            try {
                return GraphPlatform.builder(MacroBody.class)
                        .types(platform.types())
                        .define(analysisNodes)
                        .define(definitions.values())
                        .build();
            } catch (PlatformException e) {
                throw new IllegalStateException("Platform nodes do not fit macro analysis", e);
            }
        }

        private static Optional<String> name(String nodeId, NodeInstance node, List<GraphProblem> found) {
            JsonNode name = node.config().get(MacroBoundary.NAME);
            if (name == null || !name.isString() || !name.stringValue().matches("[a-z][a-z0-9_]*")) {
                found.add(GraphProblem.error(nodeId, MacroBoundary.NAME, "Needs a name made of lower-case letters, digits and underscores"));
                return Optional.empty();
            }
            return Optional.of(name.stringValue());
        }

        private static void register(Map<String, String> ports, String name, String nodeId, List<GraphProblem> found) {
            String previous = ports.putIfAbsent(name, nodeId);
            if (previous != null) found.add(GraphProblem.error(nodeId, MacroBoundary.NAME, "Name %s is already used by %s".formatted(name, previous)));
        }

        private void problem(String macro, GraphProblem problem) {
            problems.computeIfAbsent(macro, key -> new ArrayList<>()).add(problem);
        }
    }
}
