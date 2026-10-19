/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import de.eldoria.graph.definition.descriptor.NodeDescriptor;
import de.eldoria.graph.definition.descriptor.NodeKind;
import de.eldoria.graph.definition.descriptor.Shapes;
import de.eldoria.graph.definition.emit.EmitContext;
import de.eldoria.graph.definition.emit.OutputEmitter;

import java.util.Objects;

/**
 * A node computing its outputs as expressions: values and pure transformations. The compiler evaluates it when one of
 * its outputs is first read in a scope.
 *
 * @param descriptor id, editor texts and ports; kind {@link NodeKind#VALUE} or {@link NodeKind#PURE}, no executions
 * @param emitter    expression for each output
 */
public record ExpressionDefinition(NodeDescriptor descriptor, OutputEmitter<EmitContext> emitter)
        implements NodeDefinition {
    public ExpressionDefinition {
        Objects.requireNonNull(emitter, "emitter");
        Shapes.require(descriptor, NodeKind.VALUE, NodeKind.PURE);
        if (descriptor.outputs().isEmpty())
            throw new IllegalArgumentException(descriptor.id() + ": expressions need an output");
        if (!descriptor.executions().isEmpty())
            throw new IllegalArgumentException(descriptor.id() + ": expressions cannot chain nodes");
        if (descriptor.kind() == NodeKind.VALUE && !descriptor.inputs().isEmpty()) {
            throw new IllegalArgumentException(descriptor.id() + ": value nodes take no inputs");
        }
    }
}
