/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.compiler.CompiledGraph;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphCompiler;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.std.runtime.Cooldown;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Compiles and runs small graphs of standard nodes. {@code test.run} is the entry point, {@code test.record} hands a
 * value back to the test.
 */
final class Harness {
    static final EntryDefinition RUN = new EntryDefinition(
            NodeDescriptor.builder("test.run", NodeKind.ENTRY).execution("next").build(),
            "run",
            List.of(),
            (port, ctx) -> {
                throw new IllegalArgumentException(port);
            });
    static final StatementDefinition RECORD = new StatementDefinition(
            NodeDescriptor.builder("test.record", NodeKind.ACTION)
                    .input("value", Object.class)
                    .execution("next")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$L.record($L)", ctx.context(), ctx.input("value"))
                    .add(ctx.chain("next"))
                    .build());

    private Harness() {}

    private static GraphCompiler<StdHost> compiler(List<NodeDefinition> extra) {
        GraphPlatform platform = GraphPlatform.builder(StdHost.class)
                .context(Recorder.class)
                .define(StandardNodes.all())
                .define(RUN, RECORD)
                .define(extra)
                .classpath(List.of(codeSource(StdHost.class), codeSource(Cooldown.class)))
                .build();
        return new GraphCompiler<>(platform, StdHost.class, Harness.class.getClassLoader());
    }

    static CompiledGraph<StdHost> compile(Graph graph) throws GraphCompilationException {
        return compiler(List.of()).compile(graph, ClassName.get("generated.std", "Test"));
    }

    static List<Object> run(Graph graph, int times) throws GraphCompilationException {
        return run(compile(graph), times);
    }

    static List<Object> runWith(Graph graph, List<NodeDefinition> extra) throws GraphCompilationException {
        return run(compiler(extra).compile(graph, ClassName.get("generated.std", "Test")), 1);
    }

    private static List<Object> run(CompiledGraph<StdHost> compiled, int times) {
        Recorder recorder = new Recorder();
        StdHost host = compiled.instantiate(recorder);
        for (int i = 0; i < times; i++) host.run();
        return recorder.values();
    }

    /**
     * Evaluates one pure node with literal inputs.
     *
     * @param type   node type
     * @param inputs literal per input port; Integer, Double, Boolean and String become value nodes
     * @return the recorded {@code result} output
     */
    static Object evaluate(String type, Map<String, Object> inputs) throws GraphCompilationException {
        Graph.Builder graph = Graph.builder()
                .node("run", NodeInstance.of("test.run").chain("next", "record"))
                .node("record", NodeInstance.of("test.record").input("value", "node", "result"));
        NodeInstance.Builder node = NodeInstance.of(type);
        inputs.forEach((port, value) -> {
            graph.node("in_" + port, literal(value));
            node.input(port, "in_" + port, "value");
        });
        graph.node("node", node);
        return run(graph.build(), 1).getFirst();
    }

    static NodeInstance.Builder literal(Object value) {
        return switch (value) {
            case Integer integer -> NodeInstance.of("value.integer").config("value", IntNode.valueOf(integer));
            case Double number -> NodeInstance.of("value.number").config("value", DoubleNode.valueOf(number));
            case Boolean bool -> NodeInstance.of("value.boolean").config("value", BooleanNode.valueOf(bool));
            case String text -> NodeInstance.of("value.text").config("value", StringNode.valueOf(text));
            default -> throw new IllegalArgumentException("No value node for " + value);
        };
    }

    private static Path codeSource(Class<?> type) {
        try {
            return Path.of(
                    type.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
