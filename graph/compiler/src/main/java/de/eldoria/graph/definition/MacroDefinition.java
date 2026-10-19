/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import de.eldoria.graph.definition.descriptor.NodeDescriptor;
import de.eldoria.graph.definition.descriptor.NodeKind;
import de.eldoria.graph.definition.descriptor.Shapes;
import de.eldoria.graph.model.Macro;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * A user-defined macro used as a node. Never emitted itself: the compiler replaces every use with the macro body
 * before validation.
 *
 * @param descriptor  ports derived from the body's boundary nodes; kind {@link NodeKind#PURE} without a start node,
 *                    {@link NodeKind#ACTION} with one
 * @param macro       the macro
 * @param inputNodes  body node providing each input port
 * @param inputTypes  selectable type id of each input port, as its input node picks it
 * @param outputNodes body node collecting each output port
 * @param startNode   body node where the execution enters, or {@code null} for a pure macro
 * @param endNodes    body node standing for each execution port
 */
public record MacroDefinition(
        NodeDescriptor descriptor,
        Macro macro,
        Map<String, String> inputNodes,
        Map<String, String> inputTypes,
        Map<String, String> outputNodes,
        @Nullable String startNode,
        Map<String, String> endNodes)
        implements NodeDefinition {
    public MacroDefinition {
        Objects.requireNonNull(macro, "macro");
        inputNodes = Map.copyOf(inputNodes);
        inputTypes = Map.copyOf(inputTypes);
        outputNodes = Map.copyOf(outputNodes);
        endNodes = Map.copyOf(endNodes);
        Shapes.require(descriptor, NodeKind.PURE, NodeKind.ACTION);
    }
}
