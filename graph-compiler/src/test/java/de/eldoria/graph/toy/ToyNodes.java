/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.toy;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.EntryParameter;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.platform.GraphPlatform;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;

import java.util.List;

/**
 * Node set of the plain-Java test consumer. Uses no Bukkit types, which keeps the framework honest.
 */
public final class ToyNodes {
    public static final EntryDefinition START = new EntryDefinition(
            NodeDescriptor.builder("entry.start", NodeKind.ENTRY)
                    .execution("next")
                    .build(),
            "onStart",
            List.of(),
            (port, ctx) -> {
                throw new IllegalArgumentException("entry.start has no output " + port);
            });

    public static final EntryDefinition SCORE = new EntryDefinition(
            NodeDescriptor.builder("entry.score", NodeKind.ENTRY)
                    .output("points", int.class)
                    .output("player", String.class)
                    .execution("next")
                    .build(),
            "onScore",
            List.of(
                    new EntryParameter("points", PortType.of(int.class)),
                    new EntryParameter("player", PortType.of(String.class))),
            (port, ctx) -> ctx.parameter(port));

    public static final ExpressionDefinition NUMBER = new ExpressionDefinition(
            NodeDescriptor.builder("value.number", NodeKind.VALUE)
                    .config("value", double.class, DoubleNode.valueOf(0))
                    .output("value", double.class)
                    .build(),
            (port, ctx) -> ctx.config("value"));

    public static final ExpressionDefinition ADD = new ExpressionDefinition(
            NodeDescriptor.builder("math.add", NodeKind.PURE)
                    .input("a", double.class)
                    .input("b", double.class)
                    .output("result", double.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("($L + $L)", ctx.input("a"), ctx.input("b")));

    public static final StatementDefinition PRINT = new StatementDefinition(
            NodeDescriptor.builder("debug.print", NodeKind.ACTION)
                    .input("value", Object.class)
                    .execution("next")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$L.print($L)", ctx.context(), ctx.input("value"))
                    .add(ctx.chain("next"))
                    .build());

    public static final StatementDefinition BRANCH = new StatementDefinition(
            NodeDescriptor.builder("flow.branch", NodeKind.CONTROL_FLOW)
                    .input("condition", boolean.class)
                    .execution("true")
                    .execution("false")
                    .execution("next")
                    .build(),
            ctx -> CodeBlock.builder()
                    .beginControlFlow("if ($L)", ctx.input("condition"))
                    .add(ctx.chain("true"))
                    .nextControlFlow("else")
                    .add(ctx.chain("false"))
                    .endControlFlow()
                    .add(ctx.chain("next"))
                    .build());

    public static final ExpressionDefinition INTEGER = new ExpressionDefinition(
            NodeDescriptor.builder("value.integer", NodeKind.VALUE)
                    .config("value", int.class, IntNode.valueOf(0))
                    .output("value", int.class)
                    .build(),
            (port, ctx) -> ctx.config("value"));

    public static final ExpressionDefinition TEXT = new ExpressionDefinition(
            NodeDescriptor.builder("value.text", NodeKind.VALUE)
                    .config("value", String.class, null)
                    .output("value", String.class)
                    .build(),
            (port, ctx) -> ctx.config("value"));

    public static final ExpressionDefinition COUNTER = new ExpressionDefinition(
            NodeDescriptor.builder("state.counter", NodeKind.PURE)
                    .description("Counts how often it is evaluated")
                    .output("count", int.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("++$L", ctx.field("count", TypeName.INT, CodeBlock.of("0"))));

    public static final ExpressionDefinition PICK = new ExpressionDefinition(
            NodeDescriptor.builder("logic.pick", NodeKind.PURE)
                    .input("condition", boolean.class)
                    .input("when_true", PortType.variable("T"))
                    .input("when_false", PortType.variable("T"))
                    .output("result", PortType.variable("T"))
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "($L ? $L : $L)", ctx.input("condition"), ctx.input("when_true"), ctx.input("when_false")));

    public static final ExpressionDefinition GREATER = new ExpressionDefinition(
            NodeDescriptor.builder("logic.greater", NodeKind.PURE)
                    .input("a", double.class)
                    .input("b", double.class)
                    .output("result", boolean.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("($L > $L)", ctx.input("a"), ctx.input("b")));

    public static final StatementDefinition REPEAT = new StatementDefinition(
            NodeDescriptor.builder("flow.repeat", NodeKind.CONTROL_FLOW)
                    .config("times", int.class, IntNode.valueOf(3))
                    .output("index", int.class)
                    .repeatingExecution("body")
                    .execution("next")
                    .build(),
            ctx -> {
                CodeBlock index = ctx.bind("index", "body");
                return CodeBlock.builder()
                        .beginControlFlow("for (int $L = 0; $L < $L; $L++)", index, index, ctx.config("times"), index)
                        .add(ctx.chain("body"))
                        .endControlFlow()
                        .add(ctx.chain("next"))
                        .build();
            });

    public static final List<NodeDefinition> ALL =
            List.of(START, SCORE, NUMBER, INTEGER, TEXT, ADD, GREATER, PICK, COUNTER, PRINT, BRANCH, REPEAT);

    private ToyNodes() {}

    public static GraphPlatform platform() {
        return GraphPlatform.builder(ToyHost.class)
                .context(ToyContext.class)
                .define(ALL)
                .build();
    }
}
