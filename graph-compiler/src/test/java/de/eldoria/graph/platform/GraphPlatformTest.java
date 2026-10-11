/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.platform;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.EntryParameter;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.toy.ToyContext;
import de.eldoria.graph.toy.ToyHost;
import de.eldoria.graph.toy.ToyNodes;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.StringNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphPlatformTest {

    private static EntryDefinition entry(String id, String method, EntryParameter... parameters) {
        return new EntryDefinition(
                NodeDescriptor.builder(id, NodeKind.ENTRY).execution("next").build(),
                method,
                List.of(parameters),
                (port, ctx) -> CodeBlock.of("null"));
    }

    @Test
    void toyPlatformBuilds() {
        GraphPlatform platform = ToyNodes.platform();

        assertSame(ToyHost.class, platform.host());
        assertEquals(ToyContext.class, platform.context().orElseThrow());
        assertEquals(ToyNodes.ALL.size(), platform.nodes().size());
        assertSame(ToyNodes.ADD, platform.node("math.add").orElseThrow());
    }

    @Test
    void reportsEveryProblemAtOnce() {
        ExpressionDefinition objectConfig = new ExpressionDefinition(
                NodeDescriptor.builder("value.object", NodeKind.VALUE)
                        .config("value", Object.class, null)
                        .output("value", Object.class)
                        .build(),
                (port, ctx) -> ctx.config("value"));
        ExpressionDefinition badDefault = new ExpressionDefinition(
                NodeDescriptor.builder("value.bad_default", NodeKind.VALUE)
                        .config("value", double.class, StringNode.valueOf("five"))
                        .output("value", double.class)
                        .build(),
                (port, ctx) -> ctx.config("value"));

        PlatformException exception = assertThrows(PlatformException.class, () -> GraphPlatform.builder(ToyHost.class)
                .define(ToyNodes.ALL)
                .define(ToyNodes.ADD)
                .define(objectConfig, badDefault)
                .define(entry("entry.missing", "onMissing"))
                .define(entry(
                        "entry.wrong_parameters", "onScore", new EntryParameter("points", PortType.of(double.class))))
                .define(entry("entry.start_again", "onStart"))
                .build());

        List<String> problems = exception.problems();
        assertEquals(6, problems.size(), String.join("\n", problems));
        assertTrue(problems.contains("Duplicate node id math.add"));
        assertTrue(problems.stream()
                .anyMatch(p -> p.startsWith(
                        "value.object: config field 'value' has type java.lang.Object without a literal codec")));
        assertTrue(problems.stream()
                .anyMatch(p -> p.startsWith("value.bad_default: default of config field 'value' is invalid")));
        assertTrue(problems.contains("entry.missing: host ToyHost has no method onMissing()"));
        assertTrue(problems.contains("entry.wrong_parameters: host ToyHost has no method onScore(double)"));
        assertTrue(problems.contains("entry.start and entry.start_again both implement onStart"));
    }

    @Test
    void hostMustBeAnInterface() {
        assertThrows(PlatformException.class, () -> GraphPlatform.builder(ToyContext.class)
                .build());
    }
}
