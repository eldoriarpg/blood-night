/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;

import java.util.List;

/**
 * Test-only nodes producing and transforming a {@code List<String>}, to exercise type inference in loops.
 */
final class ListNodes {
    static final ExpressionDefinition SPLIT = new ExpressionDefinition(
            NodeDescriptor.builder("test.split", NodeKind.PURE)
                    .input("value", String.class)
                    .output("result", PortType.parameterized(List.class, PortType.of(String.class)))
                    .build(),
            (port, ctx) -> CodeBlock.of("$T.of($L.split(\",\"))", List.class, ctx.input("value")));
    static final ExpressionDefinition UPPER = new ExpressionDefinition(
            NodeDescriptor.builder("test.upper", NodeKind.PURE)
                    .input("value", String.class)
                    .output("result", String.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.toUpperCase()", ctx.input("value")));
    static final List<NodeDefinition> ALL = List.of(SPLIT, UPPER);

    private ListNodes() {}
}
