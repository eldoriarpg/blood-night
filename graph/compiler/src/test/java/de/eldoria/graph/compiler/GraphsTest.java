/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.Macro;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.toy.ToyContext;
import de.eldoria.graph.toy.ToyHost;
import de.eldoria.graph.toy.ToyNodes;
import de.eldoria.graph.toy.ToyPosition;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphsTest {
    private static final GraphPlatform PLATFORM = GraphPlatform.builder(ToyHost.class)
            .context(ToyContext.class)
            .define(ToyNodes.ALL)
            .define(new ExpressionDefinition(
                    NodeDescriptor.builder("debug.distance", NodeKind.PURE)
                            .input("position", ToyPosition.class)
                            .output("value", double.class)
                            .build(),
                    (port, ctx) -> CodeBlock.of("$L.x()", ctx.input("position"))))
            .build();

    @Test
    void buildsTheSameGraphAsTheModelBuilder() {
        Graphs graphs = Graphs.with(PLATFORM);
        graphs.node("start", "entry.start").chain("next", "print");
        graphs.node("two", "value.number").config("value", 2.0);
        graphs.node("sum", "math.add").input("a", "two", "value").literal("b", 3);
        graphs.node("print", "debug.print").input("value", "sum", "result");

        Graph expected = Graph.builder()
                .node("start", NodeInstance.of("entry.start").chain("next", "print"))
                .node("two", NodeInstance.of("value.number").config("value", DoubleNode.valueOf(2)))
                .node(
                        "sum",
                        NodeInstance.of("math.add").input("a", "two", "value").literal("b", IntNode.valueOf(3)))
                .node("print", NodeInstance.of("debug.print").input("value", "sum", "result"))
                .build();
        assertEquals(expected, graphs.build());
    }

    @Test
    void rejectsUnknownNamesOnTheSpot() {
        Graphs graphs = Graphs.with(PLATFORM);
        assertThrows(IllegalArgumentException.class, () -> graphs.node("x", "math.plus"));
        Graphs.NodeBuilder sum = graphs.node("sum", "math.add");
        graphs.node("two", "value.number");
        assertThrows(IllegalArgumentException.class, () -> graphs.node("sum", "math.add"));
        assertThrows(IllegalArgumentException.class, () -> sum.input("c", "two", "value"));
        assertThrows(IllegalArgumentException.class, () -> sum.input("a", "two", "result"));
        assertThrows(IllegalArgumentException.class, () -> sum.config("value", 1));
        assertThrows(IllegalArgumentException.class, () -> sum.chain("next", "two"));
    }

    @Test
    void acceptsTypedInValuesOnlyWhereThePortAllowsThem() {
        Graphs graphs = Graphs.with(PLATFORM);
        Graphs.NodeBuilder distance = graphs.node("distance", "debug.distance");
        assertThrows(IllegalArgumentException.class, () -> distance.literal("position", "text"));
        Graphs.NodeBuilder sum = graphs.node("sum", "math.add");
        assertThrows(IllegalArgumentException.class, () -> sum.literal("a", new Object()));
        sum.literal("a", 1.5f).literal("b", 2L);
    }

    @Test
    void buildReportsWhatOnlyTheWholeGraphShows() {
        Graphs graphs = Graphs.with(PLATFORM);
        graphs.node("start", "entry.start").chain("next", "print");
        graphs.node("print", "debug.print").input("value", "missing", "value");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, graphs::build);
        assertTrue(error.getMessage().contains("missing"), error.getMessage());
    }

    @Test
    void knowsMacrosAndMacroBoundaries() {
        Graphs body = Graphs.macroBody(PLATFORM, MacroLibrary.empty());
        body.node("x", "macro.input").config("type", "number").config("name", "x");
        body.node("add", "math.add").input("a", "x", "value").input("b", "x", "value");
        body.node("out", "macro.output").config("name", "result").input("value", "add", "result");
        MacroLibrary macros = MacroLibrary.compile(PLATFORM, List.of(Macro.of("double_it", body.build())));

        Graphs graphs = Graphs.with(PLATFORM, macros);
        graphs.node("start", "entry.start").chain("next", "print");
        graphs.node("twice", "macro.double_it").literal("x", 4);
        graphs.node("print", "debug.print").input("value", "twice", "result");
        assertEquals(
                StringNode.valueOf("x"), body.build().nodes().get("x").config().get("name"));
        graphs.build();
    }
}
