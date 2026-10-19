/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition.descriptor;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.ExpressionDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeDescriptorTest {

    @Test
    void defaultsFromId() {
        NodeDescriptor descriptor = NodeDescriptor.builder("math.clamp_value", NodeKind.PURE)
                .input("lower_bound", double.class)
                .output("result", double.class)
                .build();

        assertEquals("math", descriptor.category());
        assertEquals("Clamp Value", descriptor.name());
        assertEquals(
                "Lower Bound", descriptor.input("lower_bound").orElseThrow().name());
        assertTrue(descriptor.output("missing").isEmpty());
    }

    @Test
    void rejectsInvalidIds() {
        assertThrows(IllegalArgumentException.class, () -> NodeDescriptor.builder("Math.Add", NodeKind.PURE));
        assertThrows(IllegalArgumentException.class, () -> NodeDescriptor.builder("math..add", NodeKind.PURE));
        assertThrows(IllegalArgumentException.class, () -> NodeDescriptor.builder("math.add", NodeKind.PURE)
                .input("a.b", double.class));
    }

    @Test
    void rejectsDuplicatePorts() {
        assertThrows(IllegalArgumentException.class, () -> NodeDescriptor.builder("math.add", NodeKind.PURE)
                .input("a", double.class)
                .input("a", double.class)
                .build());
        assertThrows(IllegalArgumentException.class, () -> NodeDescriptor.builder("value.number", NodeKind.PURE)
                .input("value", double.class)
                .config("value", double.class, null)
                .build());
    }

    @Test
    void shapeMustFitKind() {
        NodeDescriptor action = NodeDescriptor.builder("debug.print", NodeKind.ACTION)
                .execution("next")
                .build();
        NodeDescriptor valueWithInput = NodeDescriptor.builder("value.weird", NodeKind.VALUE)
                .input("a", double.class)
                .output("b", double.class)
                .build();
        NodeDescriptor entryWithInput = NodeDescriptor.builder("entry.weird", NodeKind.ENTRY)
                .input("a", double.class)
                .execution("next")
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpressionDefinition(action, (port, ctx) -> CodeBlock.of("1")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpressionDefinition(valueWithInput, (port, ctx) -> CodeBlock.of("1")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new EntryDefinition(entryWithInput, "run", List.of(), (port, ctx) -> CodeBlock.of("1")));
    }
}
